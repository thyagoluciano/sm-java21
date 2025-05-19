package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCouponCommandHandler
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import br.com.zup.test.realwave.sales.manager.domain.rescueCoupon
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderCouponCommandHandlerTest {

    val couponService = mockk<CouponService>()
    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)
    val purchaseOrderProducer = mockk<PurchaseOrderProducer>()

    val couponCommandHandler = PurchaseOrderCouponCommandHandler(
        couponService,
        purchaseOrderProducer
    ).apply {
        this.repositoryManager = repository
    }

    init {
        every { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(any()) } returns Unit
    }

    @Test
    fun createPurchaseCoupon() {
        val couponCode = CouponCode(code = "codw")
        val customer = Customer("customer-id")
        val coupon = rescueCoupon()
        val callback = Callback("http://callbackurl.com", null)

        every { couponService.getRescueServiceCoupon(couponCode, customer) } returns coupon

        val productId = ProductId("product-id")
        val purchaseOrder = couponCommandHandler.handle(
            CreatePurchaseOrderCouponCommand(
                couponCode = couponCode,
                productId = productId,
                customer = customer,
                callback = callback
            )
        )

        verify { repository.save(purchaseOrder) }

        verify { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder) }

        assertTrue(purchaseOrder.items.size == 1)
        assertTrue(purchaseOrder.events[0] is PurchaseOrderCreated)
        assertTrue(purchaseOrder.events[1] is PurchaseOrderItemAdded)
        assertTrue(purchaseOrder.events[2] is PurchaseOrderCustomerUpdated)
        assertTrue(purchaseOrder.events[3] is PurchaseOrderCouponUpdated)
        assertTrue(purchaseOrder.events[4] is PurchaseOrderPaymentUpdated)

        assertEquals(expected = customer, actual = purchaseOrder.customer)
        assertEquals(expected = couponCode, actual = purchaseOrder.coupon)
        assertEquals(expected = Payment.couponPayment(), actual = purchaseOrder.payment)
        assertEquals(expected = callback, actual = purchaseOrder.callback)

        val item = purchaseOrder.items.first { it.catalogOfferId.value == coupon.id }
        assertEquals(expected = item.catalogOfferId.value, actual = coupon.id)
        assertEquals(expected = item.validity, actual = coupon.validity)
        assertTrue(item.offerItems.any { it.productId == productId })
        assertTrue(item.offerItems.any { it.catalogOfferItemId.value == coupon.compositionId })

    }

    @Test
    fun testUpdateCoupon() {
        val customer = Customer("customer-id")
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
            .apply {
                this.customer = customer
            }

        val coupon = CouponCode(code = "code")
        val command = UpdateCouponCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            coupon = coupon
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder
        every { couponService.validateCoupon(any(), any()) } returns coupon

        couponCommandHandler.handle(command)

        verify { repository.save(purchaseOrder, Repository.OptimisticLock.DISABLED) }
        verify { couponService.validateCoupon(coupon, customer) }

        assertEquals(expected = coupon, actual = purchaseOrder.coupon)

    }

}
