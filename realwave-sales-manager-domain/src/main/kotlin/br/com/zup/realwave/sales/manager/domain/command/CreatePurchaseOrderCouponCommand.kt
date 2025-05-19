package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.addItem
import br.com.zup.realwave.sales.manager.domain.applyUpdateCoupon
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.updateCustomer
import br.com.zup.realwave.sales.manager.domain.updatePayment

data class CreatePurchaseOrderCouponCommand(
    val purchaseOrderType: PurchaseOrderType = PurchaseOrderType.COUPON,
    val id: PurchaseOrderId = PurchaseOrderId(),
    val couponCode: CouponCode,
    val customer: Customer,
    val productId: ProductId,
    val callback: Callback? = null
) {

    fun execute(purchaseOrder: PurchaseOrder, couponService: CouponService) {
        val coupon = couponService.getRescueServiceCoupon(coupon = couponCode, customerId = customer)
        val item = coupon.createPurchaseOrderItem(productId)
        applyEventsToPurchaseOrderCoupon(
            purchaseOrder = purchaseOrder,
            item = item,
            couponCode = couponCode,
            customer = customer
        )
    }

    private fun applyEventsToPurchaseOrderCoupon(
        purchaseOrder: PurchaseOrder,
        item: Item,
        couponCode: CouponCode,
        customer: Customer
    ) {
        purchaseOrder.addItem(item = item)
        purchaseOrder.updateCustomer(customer = customer)
        purchaseOrder.applyUpdateCoupon(couponCode)
        purchaseOrder.updatePayment(Payment.couponPayment())
    }

}
