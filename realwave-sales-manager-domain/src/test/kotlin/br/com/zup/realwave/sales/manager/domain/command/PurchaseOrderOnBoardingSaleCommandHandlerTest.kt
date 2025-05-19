package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateOnBoardingSaleCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderOnBoardingSaleCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val onBoardingSaleCommandHandler = PurchaseOrderOnBoardingSaleCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateOnBoardingSale() {
        val purchaseOrder = buildPurchaseOrder()
        val onBoardingSale = OnBoardingSale(offer = CatalogOfferId("catalog-offer-id"), customFields = null)
        val command = UpdateOnBoardingSaleCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            onBoardingSale = onBoardingSale
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        onBoardingSaleCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = onBoardingSale, actual = purchaseOrder.onBoardingSale)
    }

}
