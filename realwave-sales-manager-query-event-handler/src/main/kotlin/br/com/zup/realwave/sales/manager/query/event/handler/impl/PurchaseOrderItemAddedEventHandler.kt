package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import org.springframework.stereotype.Service

@Service
class PurchaseOrderItemAddedEventHandler : BaseEventHandler<PurchaseOrderItemAdded>() {

    override fun handle(event: PurchaseOrderItemAdded, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to adding item [{}] in database for purchaseOrderId: [{}]",
            event.item,
            event.aggregateId
        )
        purchaseOrderRepository.addItem(event.aggregateId, event.item, version.value)
        log.debug("Added item [{}] in database with for purchaseOrderId: [{}]", event.item, event.aggregateId)

    }

}
