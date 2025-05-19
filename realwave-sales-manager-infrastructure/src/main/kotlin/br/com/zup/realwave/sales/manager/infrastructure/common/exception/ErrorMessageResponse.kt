package br.com.zup.realwave.sales.manager.infrastructure.common.exception

data class ErrorMessageResponse(
    val code: String? = null,
    val message: String? = null,
    val customerOrderId: String? = null,
    val cause: ErrorMessageResponse? = null,
    val detailMessage: String? = null
)
