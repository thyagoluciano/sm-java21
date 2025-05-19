package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceRemoved
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSalesForceRemovedEventHandler : BaseEventHandler<PurchaseOrderSalesForceRemoved>() {

    override fun handle(event: PurchaseOrderSalesForceRemoved, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to delete salesForce in database for purchaseOrderId: [{}]",
            event.salesForce,
            event.aggregateId
        )
        purchaseOrderRepository.updateSalesForce(
            PurchaseOrderId(event.aggregateId.value),
            event.salesForce,
            version.value
        )
        log.debug("Delete mgm in database with for purchaseOrderId: [{}]", event.salesForce, event.aggregateId)
    }

}
