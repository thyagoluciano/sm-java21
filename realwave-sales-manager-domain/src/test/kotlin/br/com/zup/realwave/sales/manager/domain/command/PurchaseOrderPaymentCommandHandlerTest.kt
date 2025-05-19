package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdatePaymentCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderPaymentCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val paymentCommandHandler = PurchaseOrderPaymentCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updatePayment() {
        val payment = Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id")))
        val purchaseOrder = buildPurchaseOrder()
        val command = UpdatePaymentCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            payment = payment
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        paymentCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = payment, actual = purchaseOrder.payment)

    }

}
