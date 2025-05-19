package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderMgmUpdatedEventHandler : BaseEventHandler<PurchaseOrderMgmUpdated>() {

    override fun handle(event: PurchaseOrderMgmUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update mgm [{}] in database for purchaseOrderId: [{}]",
            event.mgm,
            event.aggregateId
        )
        purchaseOrderRepository.updateMgm(PurchaseOrderId(event.aggregateId.value), event.mgm, version.value)
        log.debug("Updated mgm [{}] in database with for purchaseOrderId: [{}]", event.mgm, event.aggregateId)
    }

}
