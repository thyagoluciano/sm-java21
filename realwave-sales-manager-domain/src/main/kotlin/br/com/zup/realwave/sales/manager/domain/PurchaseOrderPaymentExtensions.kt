package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated

fun PurchaseOrder.updatePayment(paymentList: Payment) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderPaymentUpdated(id, paymentList))
}
