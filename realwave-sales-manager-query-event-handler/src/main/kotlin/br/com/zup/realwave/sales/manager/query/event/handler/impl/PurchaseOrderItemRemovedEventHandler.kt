package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemRemoved
import org.springframework.stereotype.Service

@Service
class PurchaseOrderItemRemovedEventHandler : BaseEventHandler<PurchaseOrderItemRemoved>() {

    override fun handle(event: PurchaseOrderItemRemoved, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to remove itemId: [{}] in database for purchaseOrderId: [{}]",
            event.itemId,
            event.aggregateId
        )
        purchaseOrderRepository.removeItem(event.aggregateId, event.itemId, version.value)
        log.debug("Removed itemId [{}] in database with for purchaseOrderId: [{}]", event.itemId, event.aggregateId)
    }

}
