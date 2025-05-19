package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated

fun PurchaseOrder.updateCustomer(customer: Customer) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderCustomerUpdated(id, customer))
}
