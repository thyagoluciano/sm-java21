package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated

fun PurchaseOrder.updateSubscription(subscription: Subscription) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderSubscriptionUpdated(id, subscription))
}
