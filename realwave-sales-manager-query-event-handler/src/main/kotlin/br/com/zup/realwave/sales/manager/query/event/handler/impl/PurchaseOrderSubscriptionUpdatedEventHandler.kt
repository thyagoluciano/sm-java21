package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSubscriptionUpdatedEventHandler : BaseEventHandler<PurchaseOrderSubscriptionUpdated>() {

    override fun handle(event: PurchaseOrderSubscriptionUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update subscription [{}] in database for purchaseOrderId: [{}]",
            event.subscription,
            event.aggregateId
        )
        purchaseOrderRepository.updateSubscription(
            PurchaseOrderId(event.aggregateId.value),
            event.subscription,
            version.value
        )
        log.debug(
            "Updated on subscription [{}] in database with for purchaseOrderId: [{}]",
            event.subscription,
            event.aggregateId
        )
    }

}
