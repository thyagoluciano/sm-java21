package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderProtocolUpdatedEventHandler : BaseEventHandler<PurchaseOrderProtocolUpdated>() {

    override fun handle(event: PurchaseOrderProtocolUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update protocol [{}] in database for purchaseOrderId: [{}]",
            event.protocol,
            event.aggregateId
        )
        purchaseOrderRepository.updateProtocol(PurchaseOrderId(event.aggregateId.value), event.protocol, version.value)
        log.debug(
            "Updated on protocol [{}] in database with for purchaseOrderId: [{}]",
            event.protocol,
            event.aggregateId
        )
    }

}
