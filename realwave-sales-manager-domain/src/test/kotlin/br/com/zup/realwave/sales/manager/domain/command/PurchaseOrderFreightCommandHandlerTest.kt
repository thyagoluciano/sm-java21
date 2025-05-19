package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateFreightCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdatePaymentCommand
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderFreightCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val freightCommandHandler = PurchaseOrderFreightCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updatefreight() {
        val purchaseOrder = buildPurchaseOrder()
        val freight = Freight(
            type = Freight.Type("BR"),
            price = Price(
                currency = "BRL",
                amount = 2990,
                scale = 2
            ),
            address = Freight.Address(
                city = "Uberlândia",
                complement = "7o. Andar",
                country = "Brazil",
                district = "Tibery",
                name = "ZUP",
                state = "MG",
                street = "Av Rondon Pacheco",
                zipCode = "38400000",
                number = "2345"
            ),
            deliveryTotalTime = Freight.DeliveryTotalTime(3)
        )

        val command = UpdateFreightCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            freight = freight
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        freightCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = freight, actual = purchaseOrder.freight)

    }

}
