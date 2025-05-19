package br.com.zup.realwave.sales.manager.infrastructure.common.exception

open class SalesManagerRPCException(val errorCode: PurchaseOrderErrorCode, val response: String) : RuntimeException()
class SalesManagerCheckoutException(errorCode: PurchaseOrderErrorCode, response: String) :
    SalesManagerRPCException(errorCode, response)

class SalesManagerCheckoutUnknownException(errorCode: PurchaseOrderErrorCode, response: String) :
    SalesManagerRPCException(errorCode, response)
