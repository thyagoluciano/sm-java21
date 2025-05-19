package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrder
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CouponErrorDecoder
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItem
import br.com.zup.realwave.sales.manager.integration.purchaseOrderOfferItemsWithProduct
import br.com.zup.rw.coupon.api.v1.representation.CouponRepresentation
import br.com.zup.rw.coupon.api.v1.representation.RewardRepresentation
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class CouponServiceTest : ItegrationBaseTest() {

    @Autowired
    private lateinit var couponApiService: CouponClientService

    @Test
    fun validatedPurchaseOrderCoupon_success() {
        val items = listOf(purchaseOrderItem(offerItems = purchaseOrderOfferItemsWithProduct()))
        val purchaseOrder = buildPurchaseOrder(items = items)
        purchaseOrder.coupon = CouponCode("2FC3CB", null)
        purchaseOrder.customer = Customer("cd770feb-214f-4130-a61c-1dec0238957f")

        val couponResponse = couponApiService.validationPurchaseOrderCoupon(purchaseOrder)

        assertNotNull(couponResponse)
    }

    @Test
    fun validateCoupon_noItemWithCompositionId() {
        val items = listOf(
            purchaseOrderItem(
                offerItems = purchaseOrderOfferItemsWithProduct(
                    catalogOfferItemId = Item.OfferItem.CatalogOfferItemId("invalid")
                )
            )
        )
        val purchaseOrder = buildPurchaseOrder(items = items)
        purchaseOrder.coupon = CouponCode("2FC3CB", null)
        purchaseOrder.customer = Customer("cd770feb-214f-4130-a61c-1dec0238957f")

        try {
            couponApiService.validationPurchaseOrderCoupon(purchaseOrder)
            fail("BusinessException expected")
        } catch (e: BusinessException) {
            assertEquals(PurchaseOrderErrorCode.COMPOSITION_ID_INVALID, e.errorCode)
        }
    }


    @Test(expected = CouponErrorDecoder.CouponValidationException::class)
    fun couponInactive() {
        val items = listOf(purchaseOrderItem(offerItems = purchaseOrderOfferItemsWithProduct()))
        val purchaseOrder = buildPurchaseOrder(items = items)
        purchaseOrder.coupon = CouponCode("inactive", null)
        purchaseOrder.customer = Customer("cd770feb-214f-4130-a61c-1dec0238957f")

        couponApiService.validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test
    fun couponIntegrationError() {
        val items = listOf(purchaseOrderItem(offerItems = purchaseOrderOfferItemsWithProduct()))
        val purchaseOrder = buildPurchaseOrder(items = items)
        purchaseOrder.coupon = CouponCode("error", null)
        purchaseOrder.customer = Customer("cd770feb-214f-4130-a61c-1dec0238957f")

        try {
            couponApiService.validationPurchaseOrderCoupon(purchaseOrder)
            fail("BusinessException expected")
        } catch (e: BusinessException) {
            assertEquals(PurchaseOrderErrorCode.COUPON_INTEGRATION_ERROR, e.errorCode)
        }
    }

    @Test
    fun validatedCoupon() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.coupon = CouponCode("2FC3CB", null)
        purchaseOrder.customer = Customer("cd770feb-214f-4130-a61c-1dec0238957f")

        val couponResponse = couponApiService.validateCoupon(purchaseOrder.coupon!!, purchaseOrder.customer!!)

        assertNotNull(couponResponse)
    }

    @Test
    fun buildDiscounts() {
        val coupon = couponApiService.buildCouponDiscount(
            validatedCoupon = CouponRepresentation(
                id = null,
                description = null,
                lifeCycle = null,
                status = null,
                terms = null,
                code = null,
                couponAmountUsed = null,
                couponType = null,
                channels = listOf(),
                reward = RewardRepresentation(
                    type = "DISCOUNT_PERCENT",
                    discounts = listOf(
                        RewardRepresentation.Discount(
                            segment = null,
                            value = RewardRepresentation.Value(
                                scale = 2,
                                currency = "PERCENT",
                                amount = 1000
                            )
                        )
                    )
                )
            )
        )
        assertNotNull(coupon)
    }

    @Test
    fun getRescueServiceCoupon_whenRewardServiceIsNull() {
        val couponCode = CouponCode("VIRAFLEX20", null)
        val customerId = Customer("fb621449-e2c6-4c22-a17c-56440582e6be")

        try {
            couponApiService.getRescueServiceCoupon(couponCode, customerId)
            fail("should throw exception")
        } catch (e: BusinessException) {
            assertNotNull(e.errorCode)
            assertEquals(PurchaseOrderErrorCode.COUPON_REWARD_SERVICE_MISSING, e.errorCode)
        }
    }

    @Test
    fun getRescueServiceCoupon_whenSuccess() {
        val couponCode = CouponCode("BONUS_15GB_MIGRACOES", null)
        val customerId = Customer("8050d268-d9e1-4e1a-99cb-2f5585e6d3eb")

        val couponResponse = couponApiService.getRescueServiceCoupon(couponCode, customerId)

        assertNotNull(couponResponse)
        assertEquals(couponCode.code, couponResponse.code)
        assertNotNull(couponResponse.id)
        assertNotNull(couponResponse.compositionId)
        assertNotNull(couponResponse.validity)
        assertNotNull(couponResponse.validity.duration)
    }

}
