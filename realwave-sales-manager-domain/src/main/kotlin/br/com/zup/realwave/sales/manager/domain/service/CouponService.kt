package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.RescueServiceCoupon

interface CouponService {

    fun validationPurchaseOrderCoupon(purchaseOrder: PurchaseOrder)

    fun validateCoupon(coupon: CouponCode, customerId: Customer): CouponCode

    fun getRescueServiceCoupon(coupon: CouponCode, customerId: Customer): RescueServiceCoupon

}
