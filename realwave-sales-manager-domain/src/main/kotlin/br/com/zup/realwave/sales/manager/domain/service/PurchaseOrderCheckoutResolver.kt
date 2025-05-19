package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

interface PurchaseOrderCheckoutResolver {

    fun resolve(purchaseOrder: PurchaseOrder): PurchaseOrderCheckout

}
