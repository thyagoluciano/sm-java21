package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.RescueServiceCoupon
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.integration.feign.apis.CouponApiService
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CouponErrorDecoder
import br.com.zup.rw.coupon.api.v1.representation.CouponRepresentation
import br.com.zup.rw.coupon.api.v1.representation.RewardRepresentation
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CouponClientService @Autowired constructor(val couponApiService: CouponApiService) : CouponService {

    private val log = LogManager.getLogger(this.javaClass)

    override fun validationPurchaseOrderCoupon(purchaseOrder: PurchaseOrder) {
        val couponResponse = validateOnCouponService(purchaseOrder.coupon!!, purchaseOrder.customer!!)
        validateCompositionId(couponResponse.reward.service?.id!!, purchaseOrder)
    }

    override fun getRescueServiceCoupon(coupon: CouponCode, customerId: Customer): RescueServiceCoupon {

        val couponResponse = couponApiService.validationCoupon(code = coupon.code, customerId = customerId.id)

        if (couponResponse.reward.service == null) {
            throw BusinessException.of(PurchaseOrderErrorCode.COUPON_REWARD_SERVICE_MISSING)
        }

        return RescueServiceCoupon(
            id = couponResponse.id!!,
            code = couponResponse.code!!,
            compositionId = couponResponse.reward.service?.id!!,
            validity = OfferValidity(
                period = "DAY",
                duration = couponResponse.reward.service?.validity,
                unlimited = false
            )
        )
    }

    override fun validateCoupon(couponCode: CouponCode, customerId: Customer): CouponCode {
        val coupon = validateStatusCoupon(couponCode, customerId)

        return CouponCode(
            code = couponCode.code,
            customFields = couponCode.customFields,
            description = coupon.description,
            reward = buildCouponDiscount(coupon)
        )
    }

    private fun validateStatusCoupon(coupon: CouponCode, customer: Customer): CouponRepresentation {
        val validatedCoupon = validateOnCouponService(coupon, customer)

        if (validatedCoupon.status != "ACTIVATED") {
            throw BusinessException.of(PurchaseOrderErrorCode.COUPON_INACTIVE)
        }
        return validatedCoupon
    }

    private fun validateOnCouponService(coupon: CouponCode, customer: Customer): CouponRepresentation {
        return try {
            log.info("Begin validate coupon in coupon api")
            val validate = couponApiService.validationCoupon(
                coupon.code,
                customer.id
            )
            log.info("End validate coupon in coupon api")
            validate
        } catch (e: CouponErrorDecoder.CouponIntegrationException) {
            throw BusinessException.of(PurchaseOrderErrorCode.COUPON_INTEGRATION_ERROR)
        }
    }

    fun buildCouponDiscount(validatedCoupon: CouponRepresentation): CouponCode.Reward? {
        return validatedCoupon.reward.type?.let {
            CouponCode.Reward(
                type = validatedCoupon.reward.type!!,
                discounts = buildDiscounts(validatedCoupon)
            )
        }
    }

    private fun validateCompositionId(compositionId: String, purchaseOrder: PurchaseOrder) {
        purchaseOrder.items.flatMap { it.offerItems }.forEach {
            if (it.catalogOfferItemId.value != compositionId) {
                throw BusinessException.of(PurchaseOrderErrorCode.COMPOSITION_ID_INVALID)
            }
        }
    }

    private fun validateSegment(segment: RewardRepresentation.Segment?) {
        if (segment != null) {
            throw BusinessException.of(PurchaseOrderErrorCode.COUPON_SEGMENT_NOT_SUPPORTED)
        }
    }

    private fun buildDiscounts(validatedCoupon: CouponRepresentation): List<CouponCode.Discount> {
        return try {
            when (validatedCoupon.reward.type) {
                CouponCode.RewardType.DISCOUNT_PERCENT.name -> {
                    validatedCoupon.reward.discounts!!.map {
                        validateSegment(it.segment)
                        CouponCode.Discount(
                            segment = it.segment?.let { segment ->
                                //TODO implement segment
                                CouponCode.Segment(
                                    id = segment.id!!,
                                    name = segment.name!!,
                                    type = segment.type!!
                                )
                            },
                            discountAsPercent = it.discountAsPercent
                        )
                    }
                }
                CouponCode.RewardType.DISCOUNT_MONEY.name -> {
                    validatedCoupon.reward.discounts!!.map {
                        validateSegment(it.segment)
                        CouponCode.Discount(
                            segment = it.segment?.let { segment ->
                                CouponCode.Segment(
                                    id = segment.id!!,
                                    name = segment.name!!,
                                    type = segment.type!!
                                )
                            },
                            discount = Price(
                                currency = it.value?.currency!!,
                                scale = it.value?.scale!!,
                                amount = it.value?.amount!!.toInt()
                            )
                        )
                    }
                }
                else -> throw BusinessException.of(PurchaseOrderErrorCode.COUPON_VALIDATION_TYPE_ERROR)
            }
        } catch (e: NullPointerException) {
            throw BusinessException.of(PurchaseOrderErrorCode.COUPON_DISCOUNT_NOT_INFORMED)
        }
    }

}
