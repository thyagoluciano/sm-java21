package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.service.CallbackService
import br.com.zup.realwave.sales.manager.infrastructure.toJsonNode
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrder
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

class CallbackServiceTest : ItegrationBaseTest() {

    @Autowired
    lateinit var callbackService: CallbackService

    @Value("\${callback.test.url}")
    private lateinit var callbackUrl: String

    @Test
    fun sendCallbackSuccess() {
        val purchaseOrder = buildPurchaseOrder(
            callbackUrl = Callback
                ("$callbackUrl/purchaseordercallback", null)
        )
        callbackService.notify(purchaseOrder)
    }

    @Test
    fun sendCallbackSuccess_WithHeaders() {
        val headers = mutableMapOf<String, String>()
        headers["x-app-key"] = "test"
        val purchaseOrder = buildPurchaseOrder(
            callbackUrl = Callback
                ("$callbackUrl/purchaseordercallback", headers.toJsonNode())
        )
        callbackService.notify(purchaseOrder)
    }

    @Test(expected = BusinessException::class)
    fun sendCallbackFailed() {
        val purchaseOrder = buildPurchaseOrder(
            callbackUrl = Callback
                ("$callbackUrl/wrongurl", null)
        )
        callbackService.notify(purchaseOrder)
    }
}
