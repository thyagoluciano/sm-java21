package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated

fun PurchaseOrder.updateProtocol(protocol: Protocol) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderProtocolUpdated(id, protocol))
}
