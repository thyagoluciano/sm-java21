package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired


class CustomerInfoClientServiceTest : ItegrationBaseTest() {

    @Autowired
    private lateinit var customerInfoClientService: CustomerInfoClientService

    @Test
    fun customerShouldBeValid() {
        customerInfoClientService.validateCustomer("fba0cbcf-71a0-40f1-a8b2-ac9661509941")
    }

    @Test(expected = BusinessException::class)
    fun customerShouldBeInvalid() {
        customerInfoClientService.validateCustomer("invalid")
    }

    @Test(expected = BusinessException::class)
    fun customerShouldBeNotFound() {
        customerInfoClientService.validateCustomer("notfound")
    }

    @Test
    fun procucthouldBeValid() {
        customerInfoClientService.validateProduct(
            "fba0cbcf-71a0-40f1-a8b2-ac9661509941",
            "14f1eda4-e223-407d-84a2-3e2871966c68"
        )
    }

    @Test(expected = BusinessException::class)
    fun productShouldBeInvalid() {
        customerInfoClientService.validateProduct("fba0cbcf-71a0-40f1-a8b2-ac9661509941", "invalid")
    }

    @Test(expected = BusinessException::class)
    fun productShouldBeNotFound() {
        customerInfoClientService.validateProduct("fba0cbcf-71a0-40f1-a8b2-ac9661509941", "notfound")
    }

}
