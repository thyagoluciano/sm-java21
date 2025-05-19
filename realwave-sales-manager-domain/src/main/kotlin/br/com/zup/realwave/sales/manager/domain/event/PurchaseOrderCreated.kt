package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType

class PurchaseOrderCreated(
    val aggregateId: AggregateId,
    val purchaseOrderType: PurchaseOrderType?,
    val callback: Callback? = null,
    val customer: Customer? = null
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.id = aggregateId
        purchaseOrder.status = PurchaseOrderStatus.OPENED
        purchaseOrder.type = purchaseOrderType
        purchaseOrder.callback = callback
        purchaseOrder.customer = customer
    }

}
