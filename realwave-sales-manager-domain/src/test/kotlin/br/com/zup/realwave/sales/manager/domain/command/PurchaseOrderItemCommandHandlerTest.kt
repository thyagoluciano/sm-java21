package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderItemCommandHandler
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import br.com.zup.test.realwave.sales.manager.domain.purchaseOrderItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderItemCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val itemCommandHandler = PurchaseOrderItemCommandHandler().apply {
        this.repositoryManager = repository
    }

    @Test
    fun addItemCommand() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        val item = purchaseOrderItem()
        val command = AddItemCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            item = item
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        itemCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertTrue(purchaseOrder.items.contains(item))
    }

    @Test
    fun updateItemCommand() {
        val item = purchaseOrderItem()
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN, items = listOf(item))
        val updatedItem = purchaseOrderItem(id = item.id)

        val command = UpdateItemCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            item = updatedItem
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        itemCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertTrue(purchaseOrder.items.contains(updatedItem))

        val purchaseOrderUpdatedItem = purchaseOrder.items.first { it.id == updatedItem.id }
        assertEquals(expected = updatedItem.catalogOfferId, actual = purchaseOrderUpdatedItem.catalogOfferId)
        assertEquals(expected = updatedItem.catalogOfferType, actual = purchaseOrderUpdatedItem.catalogOfferType)
        assertEquals(expected = updatedItem.customFields, actual = purchaseOrderUpdatedItem.customFields)
        assertEquals(expected = updatedItem.offerItems, actual = purchaseOrderUpdatedItem.offerItems)
        assertEquals(expected = updatedItem.offerFields, actual = purchaseOrderUpdatedItem.offerFields)
        assertEquals(expected = updatedItem.validity, actual = purchaseOrderUpdatedItem.validity)
        assertEquals(expected = updatedItem.price, actual = purchaseOrderUpdatedItem.price)

    }

    @Test
    fun removeItemCommand() {
        val item = purchaseOrderItem()
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN, items = listOf(item))
        val command = RemoveItemCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            itemId = item.id
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        itemCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertTrue(!purchaseOrder.items.contains(item))

    }

}
