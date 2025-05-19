package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.SalesForce

class PurchaseOrderSalesForceRemoved(
    val aggregateId: AggregateId,
    val salesForce: SalesForce?
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.salesForce = salesForce
    }

}
