package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderReasonStatusUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderReasonStatusUpdatedEventHandler : BaseEventHandler<PurchaseOrderReasonStatusUpdated>() {

    override fun handle(event: PurchaseOrderReasonStatusUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update reason status [{}] in database for purchaseOrder: [{}]",
            event.reason, event.aggregateId
        )

        purchaseOrderRepository.updatePurchaseOrderReason(
            PurchaseOrderId(event.aggregateId.value),
            event.reason,
            version.value
        )

        log.debug(
            "Updated on reason status [{}] in database with for purchaseOrder: [{}]",
            event.reason, event.aggregateId
        )
    }

}
