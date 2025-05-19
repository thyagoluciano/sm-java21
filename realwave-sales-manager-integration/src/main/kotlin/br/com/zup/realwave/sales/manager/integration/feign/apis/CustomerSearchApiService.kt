package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.realwave.sales.manager.integration.feign.response.CustomerSearchResponse
import feign.Headers
import feign.Param
import feign.RequestLine

@Headers("Accept: application/json", "Content-Type: application/json")
interface CustomerSearchApiService {

    @RequestLine("GET /api/customers/{id}")
    fun apiFindById(
        @Param("id") id: String
    ): CustomerSearchResponse

    @RequestLine("GET /api/customers/{customerId}/products/{productId}")
    fun findByCustomerIdAndProductId(
        @Param("customerId") customerId: String,
        @Param("productId") productId: String
    ): CustomerSearchResponse

}
