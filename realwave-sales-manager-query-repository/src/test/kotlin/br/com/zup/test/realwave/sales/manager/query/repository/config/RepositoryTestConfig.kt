package br.com.zup.test.realwave.sales.manager.query.repository.config

import br.com.zup.realwave.sales.manager.query.repository.config.RepositoryConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Configuration
@Import(RepositoryConfig::class)
@ActiveProfiles(profiles = ["test", "postgresql"])
@ComponentScan(basePackages = ["br.com.zup.test.realwave.sales.manager.query.repository"])
open class RepositoryTestConfig
