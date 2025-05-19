package br.com.zup.realwave.sales.manager.command.application.config

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.eventsourcing.core.RepositoryManager
import br.com.zup.realwave.common.context.web.boot.RequestResponseDumpConfig
import br.com.zup.realwave.common.context.web.boot.TrackingConfig
import br.com.zup.realwave.common.graylog.GraylogConfig
import br.com.zup.realwave.common.graylog.RewriteLogConfig
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import org.apache.log4j.LogManager
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.net.InetAddress

@SpringBootApplication
@EnableDiscoveryClient
@Configuration
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@Import(
    GraylogConfig::class,
    RewriteLogConfig::class,
    TrackingConfig::class,
    RequestResponseDumpConfig::class
)
@ComponentScan(
    basePackages = [
        "br.com.zup.realwave.sales.manager",
        "br.com.zup.realwave.common.exception.handler",
        "br.com.zup.realwave.sales.manager.infrastructure"
    ]
)
open class ApplicationConfig {

    @Bean
    open fun repositoryManager(repositories: List<Repository<PurchaseOrder>>) =
        RepositoryManager(repositories)

}

private val logger = LogManager.getLogger("br.com.zup.realwave.sales.manager.application.Application")

fun main(args: Array<String>) {
    val app = SpringApplication.run(ApplicationConfig::class.java, *args)

    val applicationName = app.environment.getProperty("spring.application.name")
    val contextPath = app.environment.getProperty("server.contextPath")
    val port = app.environment.getProperty("server.port")
    val hostAddress = InetAddress.getLocalHost().hostAddress

    logger.info(
        """|
                   |------------------------------------------------------------
                   |Application '$applicationName' is running! Access URLs:
                   |   Local:      http://127.0.0.1:$port$contextPath
                   |   External:   http://$hostAddress:$port$contextPath
                   |------------------------------------------------------------""".trimMargin()
    )

}
