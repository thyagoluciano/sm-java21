package br.com.zup.realwave.sales.manager.query.repository.liquibase

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.sales.manager.infrastructure.common.liquibase.LiquibaseHandler
import br.com.zup.realwave.sales.manager.infrastructure.common.liquibase.MultiTenantProperties
import br.com.zup.spring.tenant.TenantContextHolder
import br.com.zup.spring.tenant.TenantDiscoverer
import liquibase.exception.LiquibaseException
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class LiquibaseHandlerImpl @Autowired constructor(
    val jdbcTemplate: JdbcTemplate, val dataSource: DataSource,
    var liquibaseProperties: LiquibaseProperties,
    val resourceLoader: ResourceLoader, tenantDiscoverer: TenantDiscoverer,
    val multiTenantProperties: MultiTenantProperties
) : LiquibaseHandler {

    private companion object {
        const val TENANT_MAX_LENGTH = 63
    }

    override fun handleTenant() = applyTenant(
        "${multiTenantProperties.prefix}_${RealwaveContextHolder.getContext().organization}" +
                "_${RealwaveContextHolder.getContext().application}"
    )

    override fun handleTenant(organization: String, application: String) = applyTenant(
        multiTenantProperties.prefix +
                "_${organization}_$application"
    )

    private val tenants: HashSet<String> = HashSet(tenantDiscoverer.getTenants(multiTenantProperties.prefix).get())

    private fun applyTenant(tenant: String) {
        TenantContextHolder.set(tenant)

        if (!tenants.contains(tenant)) {
            if (tenant.length > TENANT_MAX_LENGTH) {
                throw IllegalArgumentException("Tenant $tenant size must be less than or equal $TENANT_MAX_LENGTH.")
            }

            val sql = "SELECT COUNT(schema_name) FROM information_schema.schemata WHERE schema_name = '$tenant'"

            if (jdbcTemplate.queryForObject(sql, Int::class.java) <= 0) {
                jdbcTemplate.execute("CREATE SCHEMA \"$tenant\"")
                executeLiquibase(tenant)
            }

            tenants.add(tenant)
        }
    }

    @Throws(LiquibaseException::class)
    private fun executeLiquibase(schema: String) {
        val springLiquibase = SpringLiquibase()

        springLiquibase.dataSource = dataSource
        springLiquibase.changeLog = liquibaseProperties.changeLog
        springLiquibase.contexts = liquibaseProperties.contexts
        springLiquibase.labels = liquibaseProperties.labels
        springLiquibase.defaultSchema = schema
        springLiquibase.resourceLoader = resourceLoader
        springLiquibase.setShouldRun(liquibaseProperties.isEnabled)
        springLiquibase.setRollbackFile(liquibaseProperties.rollbackFile)
        springLiquibase.setChangeLogParameters(liquibaseProperties.parameters)

        springLiquibase.afterPropertiesSet()
    }

}
