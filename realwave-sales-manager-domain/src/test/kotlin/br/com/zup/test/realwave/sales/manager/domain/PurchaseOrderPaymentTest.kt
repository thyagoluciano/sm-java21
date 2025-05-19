package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.updatePayment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderPaymentTest {

    @Test
    fun testUpdatePayment() {
        val payment = Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id")))
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.updatePayment(payment)
        assertEquals(expected = payment, actual = purchaseOrder.payment)
        assertTrue(purchaseOrder.event is PurchaseOrderPaymentUpdated)
    }

    @Test(expected = BusinessException::class)
    fun testUpdatePaymentWithInvalidPurchaseOrderStatus() {
        val payment = Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id")))
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updatePayment(payment)
    }

}
