package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCustomerUpdatedEventHandler : BaseEventHandler<PurchaseOrderCustomerUpdated>() {

    override fun handle(event: PurchaseOrderCustomerUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update customer [{}] in database for purchaseOrderId: [{}]",
            event.customer,
            event.aggregateId
        )
        purchaseOrderRepository.updateCustomer(PurchaseOrderId(event.aggregateId.value), event.customer, version.value)
        log.debug(
            "Updated on customer [{}] in database with for purchaseOrderId: [{}]",
            event.customer,
            event.aggregateId
        )
    }

}
