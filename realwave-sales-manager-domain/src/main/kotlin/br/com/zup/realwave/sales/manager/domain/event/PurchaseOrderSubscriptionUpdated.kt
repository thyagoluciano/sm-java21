package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.Subscription

class PurchaseOrderSubscriptionUpdated(
    val aggregateId: AggregateId,
    val subscription: Subscription
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.subscriptionId = subscription.toString()
    }

}
