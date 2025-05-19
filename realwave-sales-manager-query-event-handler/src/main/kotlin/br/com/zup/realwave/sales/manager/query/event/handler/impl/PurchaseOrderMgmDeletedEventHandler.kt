package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmDeleted
import org.springframework.stereotype.Service

@Service
class PurchaseOrderMgmDeletedEventHandler : BaseEventHandler<PurchaseOrderMgmDeleted>() {

    override fun handle(event: PurchaseOrderMgmDeleted, metaData: MetaData, version: AggregateVersion) {
        log.debug("Received event to delete mgm in database for purchaseOrderId: [{}]", event.mgm, event.aggregateId)
        purchaseOrderRepository.updateMgm(PurchaseOrderId(event.aggregateId.value), event.mgm, version.value)
        log.debug("Delete mgm in database with for purchaseOrderId: [{}]", event.mgm, event.aggregateId)
    }

}
