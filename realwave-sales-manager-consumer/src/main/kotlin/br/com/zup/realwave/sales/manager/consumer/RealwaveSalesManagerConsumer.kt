package br.com.zup.realwave.sales.manager.consumer

import br.com.zup.realwave.common.graylog.GraylogConfig
import br.com.zup.realwave.common.graylog.RewriteLogConfig
import org.apache.logging.log4j.LogManager
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.EnableKafka
import java.net.InetAddress

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@ComponentScan(
    basePackages = [
        "br.com.zup.realwave.sales.manager.domain",
        "br.com.zup.realwave.sales.manager.command.repository",
        "br.com.zup.realwave.sales.manager.integration",
        "br.com.zup.realwave.sales.manager.consumer"
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
@Import(
    GraylogConfig::class,
    RewriteLogConfig::class
)
open class RealwaveSalesManagerConsumer

private val logger = LogManager.getLogger("br.com.zup.realwave.sales.manager.consumer.RealwaveSalesManagerConsumer")

fun main(args: Array<String>) {

    val app = SpringApplication.run(RealwaveSalesManagerConsumer::class.java, *args)

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
