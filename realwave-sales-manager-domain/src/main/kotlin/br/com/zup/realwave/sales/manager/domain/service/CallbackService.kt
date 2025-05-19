package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

interface CallbackService {

    fun notify(purchaseOrder: PurchaseOrder)

}
