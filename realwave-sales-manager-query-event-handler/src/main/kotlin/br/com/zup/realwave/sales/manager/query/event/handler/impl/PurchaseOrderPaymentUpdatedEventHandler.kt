package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderPaymentUpdatedEventHandler : BaseEventHandler<PurchaseOrderPaymentUpdated>() {

    override fun handle(event: PurchaseOrderPaymentUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update payment [{}] in database for purchaseOrderId: [{}]",
            event.payment,
            event.aggregateId
        )
        purchaseOrderRepository.updatePayment(PurchaseOrderId(event.aggregateId.value), event.payment, version.value)
        log.debug(
            "Updated on payment [{}] in database with for purchaseOrderId: [{}]",
            event.payment,
            event.aggregateId
        )
    }

}
