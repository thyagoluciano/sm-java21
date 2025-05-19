package br.com.zup.realwave.sales.manager.integration.feign.apis

import feign.Headers
import feign.Param
import feign.RequestLine

/**
 * Created by cleber on 6/7/17.
 */
@Headers("Accept: application/json")
interface EventStoreApiService {

    @RequestLine("POST /projection/{name}/command/enable")
    fun enableProjection(@Param("name") name: String)

}
