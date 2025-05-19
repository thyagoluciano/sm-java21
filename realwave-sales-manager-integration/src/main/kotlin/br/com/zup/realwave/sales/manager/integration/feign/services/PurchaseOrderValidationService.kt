package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.hasCustomer
import br.com.zup.realwave.sales.manager.domain.service.CatalogManagerService
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.service.CustomerInfoService
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import org.springframework.stereotype.Service

/**
 * Created by Danilo Paiva on 06/06/17
 */
@Service
class PurchaseOrderValidationService(
    val memberGetMemberService: MemberGetMemberService,
    val customerInfoService: CustomerInfoService,
    val couponService: CouponService,
    val catalogManagerService: CatalogManagerService
) : PurchaseOrderValidator {

    override fun validate(purchaseOrder: PurchaseOrder): Boolean {
        //TODO: validate payment information??
        validateCustomer(purchaseOrder)

        when (purchaseOrder.type) {
            PurchaseOrderType.JOIN -> validateJoinPurchaseOrder(purchaseOrder)
            PurchaseOrderType.BUY -> validateBuyPurchaseOrder(purchaseOrder)
            PurchaseOrderType.CHANGE -> validateChangePurchaseOrder(purchaseOrder)
            PurchaseOrderType.COUPON -> validateCouponPurchaseOrder(purchaseOrder)
        }

        return true
    }

    private fun validateChangePurchaseOrder(purchaseOrder: PurchaseOrder) {
        validateOffers(purchaseOrder)
        validateItemsProductId(purchaseOrder)
        hasSubscription(purchaseOrder.subscriptionId)
    }

    private fun validateBuyPurchaseOrder(purchaseOrder: PurchaseOrder) {
        validateCoupon(purchaseOrder)
        validateOffers(purchaseOrder)
        validateItemsProductId(purchaseOrder)
        allItemsHaveProductId(purchaseOrder.items.flatMap { it.offerItems })
    }

    private fun validateJoinPurchaseOrder(purchaseOrder: PurchaseOrder) {
        memberGetMemberService.validate(purchaseOrder.mgm?.code)
        validateCoupon(purchaseOrder)
        validateOffers(purchaseOrder)
        hasNotProductId(purchaseOrder.items.flatMap { it.offerItems })
    }

    private fun validateCouponPurchaseOrder(purchaseOrder: PurchaseOrder) {
        if (purchaseOrder.customer !== null && purchaseOrder.coupon != null) {
            couponService.validationPurchaseOrderCoupon(purchaseOrder)
            allItemsHaveProductId(purchaseOrder.items.flatMap { it.offerItems })
        }
    }

    private fun validateOffers(purchaseOrder: PurchaseOrder) {
        if (purchaseOrder.items.isNotEmpty()) {
            catalogManagerService.validateOffers(purchaseOrder.items)
        }
    }

    private fun validateCustomer(purchaseOrder: PurchaseOrder) {
        if (purchaseOrder.customer !== null) {
            customerInfoService.validateCustomer(purchaseOrder.customer!!.id)
        }
    }

    private fun validateItemsProductId(purchaseOrder: PurchaseOrder) {
        purchaseOrder.customer?.let { customer ->
            val offerItems = purchaseOrder.items.flatMap { it.offerItems }
            offerItems.forEach { offerItem ->
                validateOfferItemProductId(customer, offerItem)
            }
        }
    }

    private fun validateOfferItemProductId(customer: Customer, offerItem: Item.OfferItem) {
        offerItem.productId?.let {
            customerInfoService.validateProduct(customer.id, it.value)
        }
    }

    private fun validateCoupon(purchaseOrder: PurchaseOrder) {
        if (purchaseOrder.coupon != null) {
            purchaseOrder.hasCustomer()
            couponService.validateCoupon(purchaseOrder.coupon!!, purchaseOrder.customer!!)
        }
    }

    private fun hasNotProductId(offerItems: List<Item.OfferItem>) {
        if (!offerItems.map { it.productId }.any { it != null }) return
        throw BusinessException.of("type", PurchaseOrderErrorCode.NO_NEED_PRODUCT_ID_TO_TYPE_JOIN)
    }

    private fun hasSubscription(subscriptionId: String?) {
        if (!subscriptionId.isNullOrBlank()) return
        throw BusinessException.of("type", PurchaseOrderErrorCode.SUBSCRIPTION_ID_NOT_INFORMED)
    }

    private fun allItemsHaveProductId(offerItems: List<Item.OfferItem>) {
        if (offerItems.map { it.productId }.filter { it != null }.size == offerItems.size) return
        throw BusinessException.of("type", PurchaseOrderErrorCode.ALL_ITEMS_MUST_HAVE_PRODUCTID)
    }

}
