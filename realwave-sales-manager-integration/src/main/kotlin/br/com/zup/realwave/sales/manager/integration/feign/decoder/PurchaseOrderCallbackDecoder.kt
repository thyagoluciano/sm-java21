package br.com.zup.realwave.sales.manager.integration.feign.decoder

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class PurchaseOrderCallbackDecoder : ErrorDecoder {

    private val log = LogManager.getLogger(this.javaClass)

    override fun decode(methodKey: String?, response: Response?): Exception {
        log.error("Response: {}", response)
        throw BusinessException.of(
            "PurchaseOrderCalback", PurchaseOrderErrorCode
                .PURCHASE_ORDER_CALLBACK_INTEGRATION_ERROR
        )
    }

}
