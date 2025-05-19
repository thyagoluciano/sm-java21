package br.com.zup.realwave.sales.manager.integration.feign.interceptor

import feign.auth.BasicAuthRequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Created by cleber on 6/7/17.
 */
@Component
class EventStoreBasicAuth(
    @Value("\${event.store.user}") val user: String,
    @Value("\${event.store.password}") val password: String
) : BasicAuthRequestInterceptor(user, password)
