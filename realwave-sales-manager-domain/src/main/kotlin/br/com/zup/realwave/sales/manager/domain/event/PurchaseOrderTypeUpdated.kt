package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType

class PurchaseOrderTypeUpdated(
    val aggregateId: AggregateId,
    val purchaseOrderType: PurchaseOrderType?
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.type = purchaseOrderType
    }

}
