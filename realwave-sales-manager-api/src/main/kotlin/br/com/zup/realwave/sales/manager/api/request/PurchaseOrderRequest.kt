package br.com.zup.realwave.sales.manager.api.request

import br.com.zup.realwave.sales.manager.api.request.validation.PurchaseOrderTypeValidation

@PurchaseOrderTypeValidation
data class PurchaseOrderRequest(
    val type: String? = null,
    val customer: String? = null,
    val callback: CallbackRequest? = null
)
