package br.com.zup.test.realwave.sales.manager.query.application.config

import br.com.zup.realwave.sales.manager.query.application.config.ApplicationConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ApplicationConfig::class)
open class QueryApplicationTestConfig
