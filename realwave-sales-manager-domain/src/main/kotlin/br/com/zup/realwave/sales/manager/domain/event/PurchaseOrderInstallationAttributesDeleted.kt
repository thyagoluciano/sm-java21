package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderInstallationAttributesDeleted(
    val aggregateId: AggregateId,
    val productTypeId: ProductTypeId
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.installationAttributes.remove(key = productTypeId)
    }

}
