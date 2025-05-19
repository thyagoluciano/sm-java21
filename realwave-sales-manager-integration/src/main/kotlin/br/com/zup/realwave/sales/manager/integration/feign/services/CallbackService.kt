package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.graylog.feign.RwSlf4jLogger
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.service.CallbackService
import br.com.zup.realwave.sales.manager.infrastructure.JacksonExtension
import br.com.zup.realwave.sales.manager.infrastructure.toMap
import br.com.zup.realwave.sales.manager.integration.feign.apis.PurchaseOrderCallbackApiService
import br.com.zup.realwave.sales.manager.integration.feign.decoder.PurchaseOrderCallbackDecoder
import feign.Feign
import feign.Logger
import feign.Request
import feign.RequestInterceptor
import feign.Retryer
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import org.springframework.stereotype.Service

@Service
class CallbackServiceImpl(
    val purchaseOrderCallbackDecoder: PurchaseOrderCallbackDecoder,
    val requestInterceptor: RequestInterceptor
) : CallbackService {

    companion object {
        const val TEN_SECONDS = 10000
        const val ONE_MINUTE = 60000
    }

    override fun notify(purchaseOrder: PurchaseOrder) {
        val apiService = Feign.builder()
                .encoder(JacksonEncoder(JacksonExtension.jacksonObjectMapper))
                .decoder(JacksonDecoder(JacksonExtension.jacksonObjectMapper))
                .client(OkHttpClient())
                .logger(RwSlf4jLogger())
                .logLevel(Logger.Level.FULL)
                .options(Request.Options(TEN_SECONDS, ONE_MINUTE))
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder(purchaseOrderCallbackDecoder)
                .target(PurchaseOrderCallbackApiService::class.java, purchaseOrder.callback!!.url)

        var headers = mapOf<String, Any>()
        if (null != purchaseOrder.callback!!.headers) headers = purchaseOrder.callback!!.headers!!.toMap()
        apiService.notify(PurchaseOrderCallbackApiService.PurchaseOrderCallbackRequest.of(purchaseOrder), headers)
    }

}
