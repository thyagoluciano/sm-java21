package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.common.exception.handler.to.ResourceValue
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemUpdated

fun PurchaseOrder.addItem(item: Item) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderItemAdded(aggregateId = id, item = item))
}

fun PurchaseOrder.removeItem(itemId: Item.Id) {
    verifyPurchaseOrderIsOpen()
    purchaseOrderHasItem(itemId)
    applyChange(PurchaseOrderItemRemoved(aggregateId = id, itemId = itemId))
}

fun PurchaseOrder.updateItem(item: Item) {
    verifyPurchaseOrderIsOpen()
    purchaseOrderHasItem(item.id)
    applyChange(PurchaseOrderItemUpdated(aggregateId = id, item = item))
}

internal fun PurchaseOrder.purchaseOrderHasItem(itemId: Item.Id) =
    items.find { it.id == itemId }
            ?: throw NotFoundException(ResourceValue(CatalogOfferId::class.java, itemId.value))
