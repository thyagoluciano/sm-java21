package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderDeleted
import org.springframework.stereotype.Service

@Service
class PurchaseOrderDeletedEventHandler : BaseEventHandler<PurchaseOrderDeleted>() {

    override fun handle(event: PurchaseOrderDeleted, metaData: MetaData, version: AggregateVersion) {
        log.debug("Received event to delete purchase order in database with id: [{}]", event.aggregateId)
        purchaseOrderRepository.deletePurchaseOrder(
            PurchaseOrderId(event.aggregateId.value),
            version.value
        )
        log.debug("Deleted purchase order in database with id: [{}]", event.aggregateId)
    }

}
