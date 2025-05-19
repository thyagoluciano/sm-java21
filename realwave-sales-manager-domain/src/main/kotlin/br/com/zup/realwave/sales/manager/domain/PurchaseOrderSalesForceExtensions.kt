package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated

fun PurchaseOrder.removeSalesForce() {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderSalesForceRemoved(aggregateId = id, salesForce = null))
}

fun PurchaseOrder.updateSalesForce(salesForce: SalesForce) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderSalesForceUpdated(aggregateId = id, salesForce = salesForce))
}
