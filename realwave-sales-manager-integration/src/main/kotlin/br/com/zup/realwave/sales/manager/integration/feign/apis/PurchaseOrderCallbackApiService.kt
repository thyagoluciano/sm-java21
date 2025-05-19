package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import feign.HeaderMap
import feign.Headers
import feign.RequestLine

@Headers("Accept: application/json", "Content-Type: application/json")
interface PurchaseOrderCallbackApiService {

    @RequestLine("POST")
    fun notify(customerOrderCallbackRequest: PurchaseOrderCallbackRequest, @HeaderMap headerMap: Map<String, Any>?)

    data class PurchaseOrderCallbackRequest(val purchaseOrderId: String, val status: String) {
        companion object {
            fun of(purchaseOrder: PurchaseOrder): PurchaseOrderCallbackRequest {
                return PurchaseOrderCallbackRequest(
                    purchaseOrderId = purchaseOrder.id.value,
                    status = purchaseOrder.status.name
                )
            }
        }
    }
}
