package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderStatusUpdatedEventHandler : BaseEventHandler<PurchaseOrderStatusUpdated>() {

    override fun handle(event: PurchaseOrderStatusUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update reason status [{}] in database for purchaseOrder: [{}]",
            event.status,
            event.aggregateId
        )
        purchaseOrderRepository.updateStatus(PurchaseOrderId(event.aggregateId.value), event.status, version.value)
        log.debug(
            "Updated on reason status [{}] in database with for purchaseOrder: [{}]",
            event.status,
            event.aggregateId
        )
    }

}
