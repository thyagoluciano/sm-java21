package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.addItem
import br.com.zup.realwave.sales.manager.domain.discountPrice
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.updateCoupon
import br.com.zup.realwave.sales.manager.domain.validateCoupon
import org.mockito.Mockito
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderCouponTest {

    val couponService = Mockito.mock(CouponService::class.java)

    @Test
    fun testCouponValidation() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val coupon = CouponCode("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        purchaseOrder.customer = Customer("customer-id")
        purchaseOrder.validateCoupon(coupon, couponService)
        Mockito.verify(couponService).validateCoupon(coupon, purchaseOrder.customer!!)
    }

    @Test
    fun testUpdateCoupon() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val coupon = CouponCode("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val customer = Customer("customer-id")
        purchaseOrder.customer = customer
        Mockito.`when`(couponService.validateCoupon(coupon, customer)).thenReturn(coupon)
        purchaseOrder.updateCoupon(coupon, couponService)
        assertEquals(expected = coupon, actual = purchaseOrder.coupon)
    }

    @Test(expected = BusinessException::class)
    fun testUpdateCouponWithInvalidPurchaseOrderType() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.CHANGE)
        val coupon = CouponCode("coupon-code")
        purchaseOrder.customer = Customer("customer-id")
        purchaseOrder.updateCoupon(coupon, couponService)
    }

    @Test
    fun `discount money greater than the total price`() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem(
            pricesPerPeriod = listOf()
        )
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponMoney(amount = 5000)

        val discount = purchaseOrder.discountPrice()

        assertEquals(3799, discount.amount)
    }

    @Test
    fun `discount percent greater than the total price`() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem(
            pricesPerPeriod = listOf()
        )
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponPercent(type = "DISCOUNT_PERCENT", discountAsPercent = 11000)

        val discount = purchaseOrder.discountPrice()

        assertEquals(3799, discount.amount)
    }

    @Test
    fun discountPriceMoney() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem(
            pricesPerPeriod = listOf()
        )
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponMoney()

        val discount = purchaseOrder.discountPrice()
        val fullPrice = purchaseOrder.items.sumBy { it.price.amount }
        val finalPrice = fullPrice - discount.amount

        assertEquals(1000, discount.amount)
        assertEquals(2799, finalPrice)
    }

    @Test
    fun discountEqualsFullPrice() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem()
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponMoney(amount = 3799)

        val discount = purchaseOrder.discountPrice()
        val fullPrice = purchaseOrder.items.sumBy { it.firstPeriodPrice() }
        val finalPrice = fullPrice - discount.amount

        assertEquals(200, discount.amount)
        assertEquals(0, finalPrice)
    }

    @Test
    fun discountEqualsPricePerPeriod() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem(
            pricesPerPeriod = listOf()
        )
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponMoney(amount = 3799)

        val discount = purchaseOrder.discountPrice()
        val fullPrice = purchaseOrder.items.sumBy { it.firstPeriodPrice() }
        val finalPrice = fullPrice - discount.amount

        assertEquals(3799, discount.amount)
        assertEquals(0, finalPrice)
    }

    @Test
    fun discountPricePercent() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem(
            pricesPerPeriod = listOf()
        )
        purchaseOrder.addItem(item)
        purchaseOrder.coupon = purchaseOrderCouponPercent("DISCOUNT_PERCENT")

        val discount = purchaseOrder.discountPrice()

        val discountPrice = Price(
            amount = 379,
            currency = "BRL",
            scale = 2
        )

        assertEquals(discountPrice.amount, discount.amount)
    }

}
