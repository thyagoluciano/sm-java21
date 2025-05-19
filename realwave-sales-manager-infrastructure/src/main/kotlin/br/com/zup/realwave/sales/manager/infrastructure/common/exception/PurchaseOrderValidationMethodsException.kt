package br.com.zup.realwave.sales.manager.infrastructure.common.exception




class PurchaseOrderValidationMethodsException(val errorCode: PurchaseOrderErrorCode,val methods: List<PaymentMethod>) : RuntimeException() {
    data class PaymentMethod(
            val method: String?,
            val methodId: String?
    )
}


