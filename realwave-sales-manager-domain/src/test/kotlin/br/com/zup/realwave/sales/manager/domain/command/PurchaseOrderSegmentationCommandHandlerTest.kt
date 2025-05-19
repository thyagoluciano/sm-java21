package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.command.UpdateSegmentationCommand
import br.com.zup.realwave.sales.manager.infrastructure.toJsonNode
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.stereotype.Service
import kotlin.test.Test
import kotlin.test.assertEquals

@Service
class PurchaseOrderSegmentationCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val segmentationCommandHandler = PurchaseOrderSegmentationCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateSegmentation() {
        val purchaseOrder = buildPurchaseOrder()
        val segmentation = Segmentation(mapOf("aaa" to "bbb").toJsonNode())
        val command = UpdateSegmentationCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            segmentation = segmentation
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        segmentationCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = segmentation, actual = purchaseOrder.segmentation)
    }

}
