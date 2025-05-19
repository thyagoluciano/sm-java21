package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderFreightUpdatedEventHandler : BaseEventHandler<PurchaseOrderFreightUpdated>() {

    override fun handle(event: PurchaseOrderFreightUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update freight [{}] in database for purchaseOrderId: [{}]",
            event.freight,
            event.aggregateId
        )
        purchaseOrderRepository.updateFreight(PurchaseOrderId(event.aggregateId.value), event.freight, version.value)
        log.debug(
            "Updated on freight [{}] in database with for purchaseOrderId: [{}]",
            event.freight,
            event.aggregateId
        )
    }

}
