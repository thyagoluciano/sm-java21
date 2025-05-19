package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderOnBoardingSaleUpdated(
    val aggregateId: AggregateId,
    val onBoardingSale: OnBoardingSale
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.onBoardingSale = onBoardingSale
    }

}
