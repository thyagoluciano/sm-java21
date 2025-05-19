package br.com.zup.realwave.sales.manager.query.application.config

import br.com.zup.realwave.common.context.web.boot.TrackingConfig
import br.com.zup.realwave.common.graylog.GraylogConfig
import br.com.zup.realwave.sales.manager.query.event.handler.subscriber.PurchaseOrderAggregateSubscriber
import br.com.zup.realwave.sales.manager.query.repository.config.RepositoryConfig
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import javax.annotation.PostConstruct

@SpringBootApplication
@Configuration
@EnableDiscoveryClient
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@Import(
    GraylogConfig::class,
    RepositoryConfig::class,
    TrackingConfig::class
)
@ComponentScan(
    basePackages = [
        "br.com.zup.realwave.sales.manager",
        "br.com.zup.realwave.common.exception.handler",
        "br.com.zup.realwave.sales.manager.infrastructure",
        "br.com.zup.eventsourcing.eventstore"
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [
                "br\\.com\\.zup\\.realwave\\.sales\\.manager\\.domain\\.commandhandler\\..*"
            ]
        )
    ]
)
@EnableAutoConfiguration(exclude = [(LiquibaseAutoConfiguration::class)])
open class ApplicationConfig {

    @Autowired
    lateinit var subscriber: PurchaseOrderAggregateSubscriber

    @PostConstruct
    fun initSubscribe() {
        subscriber.start()
    }

}
