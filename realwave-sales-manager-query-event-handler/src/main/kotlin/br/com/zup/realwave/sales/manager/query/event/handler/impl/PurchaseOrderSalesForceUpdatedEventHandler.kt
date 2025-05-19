package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSalesForceUpdatedEventHandler : BaseEventHandler<PurchaseOrderSalesForceUpdated>() {

    override fun handle(event: PurchaseOrderSalesForceUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update salesForce in database for purchaseOrderId: [{}]",
            event.salesForce,
            event.aggregateId
        )
        purchaseOrderRepository.updateSalesForce(
            PurchaseOrderId(event.aggregateId.value),
            event.salesForce,
            version.value
        )
        log.debug("Updated mgm in database with for purchaseOrderId: [{}]", event.salesForce, event.aggregateId)
    }

}
