package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateProtocolCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderProtocolCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val protocolCommandHandler = PurchaseOrderProtocolCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateProtocol() {
        val purchaseOrder = buildPurchaseOrder()
        val protocol = Protocol("protocol")
        val command = UpdateProtocolCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            protocol = protocol
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        protocolCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = protocol.value, actual = purchaseOrder.protocol)
    }

}
