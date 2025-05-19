package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated

fun PurchaseOrder.updateInstallationAttributes(installationAttribute: InstallationAttribute) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderInstallationAttributesUpdated(id, installationAttribute))
}

fun PurchaseOrder.deleteInstallationAttributes(productTypeId: ProductTypeId) {
    verifyPurchaseOrderIsOpen()
    verifyInstallationAttributesConstainsProductTypeId(productTypeId)
    applyChange(PurchaseOrderInstallationAttributesDeleted(aggregateId = id, productTypeId = productTypeId))
}

fun PurchaseOrder.installationAttributesFor(productTypeId: String): Map<String, Any> =
    installationAttributes[ProductTypeId(productTypeId)]?.attributes ?: emptyMap()
