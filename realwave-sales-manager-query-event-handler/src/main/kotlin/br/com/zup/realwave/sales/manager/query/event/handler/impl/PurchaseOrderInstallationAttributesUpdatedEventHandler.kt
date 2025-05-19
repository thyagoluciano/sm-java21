package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderInstallationAttributesUpdatedEventHandler
    : BaseEventHandler<PurchaseOrderInstallationAttributesUpdated>() {

    override fun handle(
        event: PurchaseOrderInstallationAttributesUpdated,
        metaData: MetaData,
        version: AggregateVersion
    ) {
        log.debug(
            "Received event to update installation attributes [{}] in database for purchaseOrderId: [{}]",
            event.installationAttribute,
            event.aggregateId
        )
        purchaseOrderRepository.updateInstallationAttributes(
            event.aggregateId.value,
            event.installationAttribute,
            version.value
        )
        log.debug(
            "Updated installation attributes [{}] in database with for purchaseOrderId: [{}]",
            event.installationAttribute,
            event.aggregateId
        )
    }

}
