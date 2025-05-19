package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.command.RemoveSalesForceCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateSalesForceCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PurchaseOrderSalesForceCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val salesForceCommandHandler = PurchaseOrderSalesForceCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateSalesForce() {
        val purchaseOrder = buildPurchaseOrder()
        val salesForce = SalesForce(
            id = "sales-force-id",
            name = "sales-force-name"
        )
        val command = UpdateSalesForceCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            salesForce = salesForce
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        salesForceCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = salesForce, actual = purchaseOrder.salesForce)
    }

    @Test
    fun removeSalesForce() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.salesForce = SalesForce(
            id = "sales-force-id",
            name = "sales-force-name"
        )
        val command = RemoveSalesForceCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString())
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        salesForceCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertNull(purchaseOrder.salesForce)
    }

}
