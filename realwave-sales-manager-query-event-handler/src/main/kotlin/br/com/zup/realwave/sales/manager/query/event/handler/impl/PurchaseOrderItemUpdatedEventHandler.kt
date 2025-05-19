package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderItemUpdatedEventHandler : BaseEventHandler<PurchaseOrderItemUpdated>() {

    override fun handle(event: PurchaseOrderItemUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update item [{}] in database for purchaseOrderId: [{}]",
            event.item,
            event.aggregateId
        )
        purchaseOrderRepository.updateItem(event.aggregateId, event.item, version.value)
        log.debug("Updated item [{}] in database with for purchaseOrderId: [{}]", event.item, event.aggregateId)
    }

}
