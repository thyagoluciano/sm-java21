package br.com.zup.realwave.sales.manager.integration.feign.decoder

import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class CouponErrorDecoder : ErrorDecoder {

    private val log = LogManager.getLogger(this.javaClass)

    override fun decode(methodKey: String?, response: Response?): Exception {
        return when {
            (response!!.status() == HttpStatus.BAD_REQUEST.value()) -> {
                CouponValidationException(
                    errorCode = PurchaseOrderErrorCode.COUPON_INTEGRATION_ERROR,
                    response = String(response.body().asInputStream().readBytes())
                )
            }
            else -> {
                CouponIntegrationException()
            }
        }
    }

    class CouponIntegrationException : RuntimeException()
    class CouponValidationException(val errorCode: PurchaseOrderErrorCode, val response: String) : RuntimeException()
}
