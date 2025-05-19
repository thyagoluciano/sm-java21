package br.com.zup.test.realwave.sales.manager.application.config

import br.com.zup.realwave.sales.manager.command.application.config.ApplicationConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * Created by danilo on 24/05/17.
 */
@Configuration
@Import(ApplicationConfig::class)
open class ApplicationTestConfig
