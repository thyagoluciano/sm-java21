package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesDeleted
import org.springframework.stereotype.Service

@Service
class PurchaseOrderInstallationAttributesDeletedEventHandler
    : BaseEventHandler<PurchaseOrderInstallationAttributesDeleted>() {

    override fun handle(
        event: PurchaseOrderInstallationAttributesDeleted,
        metaData: MetaData,
        version: AggregateVersion
    ) {
        log.debug(
            "Received event to delete installation attributes [{}] in database for purchaseOrderId: [{}]",
            event.productTypeId,
            event.aggregateId
        )
        purchaseOrderRepository.deleteInstallationAttributes(
            event.aggregateId.value,
            event.productTypeId,
            version.value
        )
        log.debug(
            "Deleted installation attributes [{}] in database with for purchaseOrderId: [{}]",
            event.productTypeId,
            event.aggregateId
        )
    }

}
