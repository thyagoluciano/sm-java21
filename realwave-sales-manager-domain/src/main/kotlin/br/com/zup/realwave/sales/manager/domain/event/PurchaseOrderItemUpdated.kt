package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderItemUpdated(
    val aggregateId: AggregateId,
    val item: Item
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.items.remove(item)
        purchaseOrder.items.add(item)
    }

}
