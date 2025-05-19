package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderOnBoardingSaleUpdated

fun PurchaseOrder.updateOnBoardingSale(onBoardingSale: OnBoardingSale) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderOnBoardingSaleUpdated(id, onBoardingSale))
}
