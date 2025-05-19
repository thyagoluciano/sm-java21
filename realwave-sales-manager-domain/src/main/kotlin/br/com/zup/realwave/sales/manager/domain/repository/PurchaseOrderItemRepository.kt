package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId

/**
 * Created by marcosgm on 05/06/17
 */
interface PurchaseOrderItemRepository {
    fun addItem(purchaseOrderId: AggregateId, item: Item): Int
    fun findOne(purchaseOrderId: PurchaseOrderId, itemId: Item.Id): Item?
    fun removeItem(purchaseOrderId: AggregateId, itemId: Item.Id): Int
    fun updateItem(purchaseOrderId: AggregateId, item: Item): Int
    fun findByPurchaseOrderId(purchaseOrderId: PurchaseOrderId): List<Item>
}
