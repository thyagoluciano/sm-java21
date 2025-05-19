package br.com.zup.realwave.sales.manager.integration.config

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [(IntegrationTestConfig::class)])
abstract class ItegrationBaseTest
