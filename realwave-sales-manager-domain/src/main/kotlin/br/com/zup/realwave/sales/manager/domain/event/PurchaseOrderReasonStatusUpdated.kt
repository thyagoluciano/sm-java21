package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.Reason

class PurchaseOrderReasonStatusUpdated(
    val aggregateId: AggregateId,
    val reason: Reason?
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.reason = reason
    }

}
