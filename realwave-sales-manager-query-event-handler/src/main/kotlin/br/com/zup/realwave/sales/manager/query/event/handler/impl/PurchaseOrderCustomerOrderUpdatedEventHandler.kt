package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerOrderUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCustomerOrderUpdatedEventHandler : BaseEventHandler<PurchaseOrderCustomerOrderUpdated>() {

    override fun handle(event: PurchaseOrderCustomerOrderUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug("Received event to update customer order status in database with id: [{}]", event.aggregateId)

        if (event.customerOrder != null) {
            customerOrderRepository.saveCustomerOrder(event.aggregateId, event.customerOrder)
        }

        purchaseOrderRepository.updateVersion(event.aggregateId, version.value)
        log.debug("Applied update to customer order status in database with id: [{}]", event.aggregateId)
    }

}
