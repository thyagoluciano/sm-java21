package br.com.zup.realwave.sales.manager.integration.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["br.com.zup.realwave.sales.manager.integration"])
open class IntegrationTestConfig
