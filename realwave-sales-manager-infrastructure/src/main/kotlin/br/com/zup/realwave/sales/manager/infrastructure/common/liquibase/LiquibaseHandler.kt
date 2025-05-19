package br.com.zup.realwave.sales.manager.infrastructure.common.liquibase

/**
 * Created by marcosgm on 06/07/17
 */
interface LiquibaseHandler {

    fun handleTenant()
    fun handleTenant(organization: String, application: String)

}
