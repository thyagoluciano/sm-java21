package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderInstallationAttributesUpdated(
    val aggregateId: AggregateId,
    val installationAttribute: InstallationAttribute
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.installationAttributes[installationAttribute.productTypeId] = installationAttribute
    }

}
