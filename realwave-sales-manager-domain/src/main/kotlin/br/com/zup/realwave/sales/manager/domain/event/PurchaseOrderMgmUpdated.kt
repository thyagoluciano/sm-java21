package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderMgmUpdated(
    val aggregateId: AggregateId,
    val mgm: Mgm
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.mgm = mgm
    }

}
