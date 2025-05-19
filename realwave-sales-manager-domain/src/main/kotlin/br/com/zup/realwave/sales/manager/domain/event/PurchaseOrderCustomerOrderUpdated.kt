package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderCustomerOrderUpdated(
    val aggregateId: AggregateId,
    val customerOrder: CustomerOrder
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.customerOrder = customerOrder
    }

}
