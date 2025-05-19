package br.com.zup.test.realwave.sales.manager.query.application.config

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [(QueryApplicationTestConfig::class)])
abstract class QueryApplicationBaseTest
