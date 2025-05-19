package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.Subscription

data class UpdateSubscriptionCommand(
    val id: PurchaseOrderId,
    val subscriptionId: Subscription
)
