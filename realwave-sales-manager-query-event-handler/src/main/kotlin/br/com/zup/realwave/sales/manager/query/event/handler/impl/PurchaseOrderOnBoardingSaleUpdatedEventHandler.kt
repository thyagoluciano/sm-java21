package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderOnBoardingSaleUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderOnBoardingSaleUpdatedEventHandler : BaseEventHandler<PurchaseOrderOnBoardingSaleUpdated>() {

    override fun handle(event: PurchaseOrderOnBoardingSaleUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update on boarding sale [{}] in database for purchaseOrderId: [{}]",
            event.onBoardingSale,
            event.aggregateId
        )
        purchaseOrderRepository.updateOnBoardingSale(
            PurchaseOrderId(event.aggregateId.value),
            event.onBoardingSale,
            version.value
        )
        log.debug(
            "Updated on boarding sale [{}] in database with for purchaseOrderId: [{}]",
            event.onBoardingSale,
            event.aggregateId
        )
    }

}
