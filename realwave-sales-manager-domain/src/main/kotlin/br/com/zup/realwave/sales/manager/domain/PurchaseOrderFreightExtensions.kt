package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated

fun PurchaseOrder.updateFreight(freight: Freight) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderFreightUpdated(id, freight))
}
