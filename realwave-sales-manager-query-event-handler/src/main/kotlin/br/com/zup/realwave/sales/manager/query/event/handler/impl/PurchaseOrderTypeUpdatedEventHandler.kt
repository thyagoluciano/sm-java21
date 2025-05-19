package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderTypeUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderTypeUpdatedEventHandler : BaseEventHandler<PurchaseOrderTypeUpdated>() {

    override fun handle(event: PurchaseOrderTypeUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update subscription [{}] in database for purchaseOrderId: [{}]",
            event.purchaseOrderType,
            event.aggregateId
        )
        purchaseOrderRepository.updatePurchaseOrderType(
            PurchaseOrderId(event.aggregateId.value),
            event.purchaseOrderType,
            version.value
        )
        log.debug(
            "Updated on subscription [{}] in database with for purchaseOrderId: [{}]",
            event.purchaseOrderType,
            event.aggregateId
        )
    }

}
