package br.com.zup.realwave.sales.manager.infrastructure.common.liquibase

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by branquinho on 05/06/17.
 */
@ConfigurationProperties(prefix = "tenant", ignoreUnknownFields = true)
open class MultiTenantProperties {

    var prefix: String = ""
        get() = field.toLowerCase()

}