package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCustomerCommandHandler
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderCustomerCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val customerCommandHandler = PurchaseOrderCustomerCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateCustomer() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        val customer = Customer("customer")
        val command = UpdateCustomerCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            customer = customer
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = customer, actual = purchaseOrder.customer)
    }

}
