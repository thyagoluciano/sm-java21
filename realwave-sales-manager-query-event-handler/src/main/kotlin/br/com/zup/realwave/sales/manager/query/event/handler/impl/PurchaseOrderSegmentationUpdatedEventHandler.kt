package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSegmentationUpdatedEventHandler : BaseEventHandler<PurchaseOrderSegmentationUpdated>() {

    override fun handle(event: PurchaseOrderSegmentationUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update segmentation [{}] in database for purchaseOrderId: [{}]",
            event.segmentation,
            event.aggregateId
        )
        purchaseOrderRepository.updateSegmentation(
            PurchaseOrderId(event.aggregateId.value),
            event.segmentation,
            version.value
        )
        log.debug(
            "Updated segmentation [{}] in database with for purchaseOrderId: [{}]",
            event.segmentation,
            event.aggregateId
        )
    }

}
