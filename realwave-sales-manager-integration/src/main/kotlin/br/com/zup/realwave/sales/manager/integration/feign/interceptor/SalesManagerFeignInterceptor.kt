package br.com.zup.realwave.sales.manager.integration.feign.interceptor

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * Created by Kings on 7/6/17.
 */
@Component
@Primary
class SalesManagerFeignInterceptor(val counterService: CounterService) : RequestInterceptor {

    @Value("\${gateway.application.key:}")
    private lateinit var gatewayApplicationKey: String

    @Value("\${catalog.search.ssl.auth:}")
    private lateinit var catalogSearchSslAuth: String

    companion object {
        const val GATEWAY_APP_KEY_HEADER = "x-application-key"
        const val CATALOG_SEARCH_SSL_AUTH = "x-ssl-auth"
    }

    override fun apply(template: RequestTemplate) {
        if (!gatewayApplicationKey.isBlank()) {
            template.header(GATEWAY_APP_KEY_HEADER, gatewayApplicationKey)
        }
        if (!catalogSearchSslAuth.isBlank()) {
            template.header(CATALOG_SEARCH_SSL_AUTH, catalogSearchSslAuth)
        }

        counterService.increment("feign.req.${template.method().toLowerCase()}${template.url().replace("/", ".")}")
    }
}
