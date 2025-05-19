package br.com.zup.realwave.sales.manager.query.repository.liquibase

import br.com.zup.realwave.sales.manager.infrastructure.common.liquibase.MultiTenantProperties
import br.com.zup.spring.tenant.TenantDiscoverer
import liquibase.integration.spring.MultiTenantSpringLiquibase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(MultiTenantProperties::class, LiquibaseProperties::class)
open class LiquibaseConfig @Autowired constructor(
    val datasource: DataSource, val liquibaseProperties: LiquibaseProperties,
    val tenantDiscoverer: TenantDiscoverer, val multiTenantProperties: MultiTenantProperties
) {

    private val logger = LoggerFactory.getLogger(LiquibaseConfig::class.java)

    @Bean
    open fun liquibase(): MultiTenantSpringLiquibase {
        val liquibase = MultiTenantSpringLiquibase()

        liquibase.dataSource = datasource
        liquibase.changeLog = liquibaseProperties.changeLog
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.labels = liquibaseProperties.labels
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        liquibase.isShouldRun = liquibaseProperties.isEnabled
        liquibase.schemas = tenantDiscoverer.getTenants(multiTenantProperties.prefix).get() +
                liquibaseProperties.defaultSchema

        logger.debug("Configuring Liquibase")

        return liquibase
    }

}
