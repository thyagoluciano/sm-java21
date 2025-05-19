package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.command.UpdateSubscriptionCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.stereotype.Service
import kotlin.test.Test
import kotlin.test.assertEquals

@Service
class PurchaseOrderSubscriptionCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val subscriptionCommandHandler = PurchaseOrderSubscriptionCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateSubscription() {
        val purchaseOrder = buildPurchaseOrder()
        val subscription = Subscription("subscription-id")
        val command = UpdateSubscriptionCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            subscriptionId = subscription
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        subscriptionCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = subscription.id, actual = purchaseOrder.subscriptionId)
    }

}
