package br.com.zup.test.realwave.sales.manager.query.repository.config

import br.com.zup.spring.tenant.TenantContextHolder
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@RunWith(org.springframework.test.context.junit4.SpringRunner::class)
@ContextConfiguration(classes = [(RepositoryTestConfig::class)])
abstract class RepositoryBaseTest {

    init {
        TenantContextHolder.set("public")
    }

    fun logAndFail(message: String, logger: org.slf4j.Logger) {
        logger.error(message)

        org.junit.Assert.fail(message)
    }

    fun logAndFail(message: String, e: Exception, logger: org.slf4j.Logger) {
        logger.error(message, e)

        org.junit.Assert.fail(message)
    }

}
