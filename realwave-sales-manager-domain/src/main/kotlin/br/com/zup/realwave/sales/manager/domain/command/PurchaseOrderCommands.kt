package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

interface PurchaseOrderCommand {

    fun execute(purchaseOrder: PurchaseOrder)

}
