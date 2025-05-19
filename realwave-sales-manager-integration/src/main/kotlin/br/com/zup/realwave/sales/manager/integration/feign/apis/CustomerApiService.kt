package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.realwave.sales.manager.integration.feign.response.CustomerSearchResponse
import feign.Headers
import feign.Param
import feign.RequestLine

@Headers("Accept: application/json", "Content-Type: application/json")
interface CustomerApiService {

    @RequestLine("GET /v1/customers/{id}")
    fun findCustomerById(
        @Param("id") id: String
    ): CustomerSearchResponse

}
