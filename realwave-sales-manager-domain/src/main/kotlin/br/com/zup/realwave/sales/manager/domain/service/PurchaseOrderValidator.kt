package br.com.zup.realwave.sales.manager.domain.service

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

/**
 * Created by Danilo Paiva on 06/06/17
 */
interface PurchaseOrderValidator {

    fun validate(purchaseOrder: PurchaseOrder): Boolean

}
