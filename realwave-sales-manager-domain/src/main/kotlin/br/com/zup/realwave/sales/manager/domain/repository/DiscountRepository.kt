package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.realwave.sales.manager.domain.CouponCode

interface DiscountRepository {

    fun findByCoupon(coupon: CouponCode): CouponCode.Reward?

    fun saveDiscount(coupon: CouponCode): Int

}
