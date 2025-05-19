package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.SecurityCode

interface PurchaseOrderCheckout {

    fun checkout(
        purchaseOrder: PurchaseOrder,
        channel: Channel,
        securityCode: List<SecurityCode>? = emptyList()
    ): CustomerOrder?
}
