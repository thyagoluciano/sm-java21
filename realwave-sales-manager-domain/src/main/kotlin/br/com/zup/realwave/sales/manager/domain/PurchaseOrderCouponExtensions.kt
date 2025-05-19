package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.service.CouponService

fun PurchaseOrder.updateCoupon(couponCode: CouponCode, couponService: CouponService) {
    validatePurchaseOrderTypeForCoupon()
    hasCustomer()
    val coupon = validateCoupon(couponCode, couponService)
    applyUpdateCoupon(coupon)
}

fun PurchaseOrder.applyUpdateCoupon(coupon: CouponCode) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderCouponUpdated(id, coupon))
}
