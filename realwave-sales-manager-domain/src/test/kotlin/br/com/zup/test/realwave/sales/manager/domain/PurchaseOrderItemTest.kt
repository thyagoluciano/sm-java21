package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.addItem
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemUpdated
import br.com.zup.realwave.sales.manager.domain.removeItem
import br.com.zup.realwave.sales.manager.domain.updateItem
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderItemTest {

    @Test
    fun addItem() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val item = purchaseOrderItem()
        purchaseOrder.addItem(item)
        assertTrue(purchaseOrder.items.contains(item))
        assertTrue(purchaseOrder.items.first().offerItems.isNotEmpty())
        assertTrue(purchaseOrder.event is PurchaseOrderItemAdded)
        assertTrue(purchaseOrder.items.first().pricesPerPeriod.isNotEmpty())
    }

    @Test(expected = BusinessException::class)
    fun addItemWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        val item = purchaseOrderItem()
        purchaseOrder.addItem(item)
    }

    @Test
    fun updateItem() {
        val purchaseOrder = PurchaseOrder()
        val purchaseOrderId = PurchaseOrderId()

        val item = purchaseOrderItem()
        val updatedItem = purchaseOrderItem(id = item.id, pricesPerPeriod = listOf())

        purchaseOrder.loadEvents(
            listOf(
                PurchaseOrderCreated(purchaseOrderId, PurchaseOrderType.JOIN),
                PurchaseOrderItemAdded(purchaseOrderId, item)
            )
        )

        assertTrue(purchaseOrder.items.first().pricesPerPeriod.isNotEmpty())

        purchaseOrder.updateItem(updatedItem)

        val purchaseOrderUpdatedItem = purchaseOrder.items.first { it.id == updatedItem.id }

        assertEquals(expected = updatedItem, actual = purchaseOrderUpdatedItem)
        assertEquals(expected = updatedItem.catalogOfferId, actual = purchaseOrderUpdatedItem.catalogOfferId)

        assertTrue(purchaseOrder.items.size == 1)
        assertTrue(purchaseOrder.event is PurchaseOrderItemUpdated)
        assertTrue(purchaseOrder.items.first().pricesPerPeriod.isEmpty())
    }

    @Test(expected = NotFoundException::class)
    fun updateInvalidItem() {
        val purchaseOrder = PurchaseOrder()
        val purchaseOrderId = PurchaseOrderId()
        val item = purchaseOrderItem()

        val updatedItem = purchaseOrderItem(id = Item.Id("invalid-item"))

        purchaseOrder.loadEvents(
            listOf(
                PurchaseOrderCreated(purchaseOrderId, PurchaseOrderType.JOIN),
                PurchaseOrderItemAdded(purchaseOrderId, item)
            )
        )

        purchaseOrder.updateItem(updatedItem)
    }

    @Test(expected = BusinessException::class)
    fun updateItemWithInvalidPurchaseOrderStatus() {
        val item = purchaseOrderItem()
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED, items = listOf(item))
        val updatedItem = purchaseOrderItem(id = item.id)
        purchaseOrder.updateItem(updatedItem)
    }

    @Test
    fun removeItem() {
        val purchaseOrder = PurchaseOrder()
        val purchaseOrderId = PurchaseOrderId()
        val item = purchaseOrderItem()

        purchaseOrder.load(
            listOf(
                PurchaseOrderCreated(purchaseOrderId, PurchaseOrderType.JOIN),
                PurchaseOrderItemAdded(purchaseOrderId, item)
            ), AggregateVersion(2)
        )
        assertTrue(purchaseOrder.items.size == 1)

        purchaseOrder.removeItem(item.id)

        assertTrue(purchaseOrder.event is PurchaseOrderItemRemoved)
        assertTrue(purchaseOrder.items.size == 0)
    }

    @Test(expected = NotFoundException::class)
    fun removeInvalidItem() {
        val purchaseOrder = PurchaseOrder()
        val purchaseOrderId = PurchaseOrderId()
        val item = purchaseOrderItem()
        purchaseOrder.load(
            listOf(
                PurchaseOrderCreated(purchaseOrderId, PurchaseOrderType.JOIN),
                PurchaseOrderItemAdded(purchaseOrderId, item)
            ), AggregateVersion(2)
        )
        purchaseOrder.removeItem(Item.Id("invalid-item"))
    }

    @Test(expected = BusinessException::class)
    fun removeItemWithInvalidPurchaseOrderStatus() {
        val item = purchaseOrderItem()
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED, items = listOf(item))
        purchaseOrder.removeItem(item.id)
    }

}
