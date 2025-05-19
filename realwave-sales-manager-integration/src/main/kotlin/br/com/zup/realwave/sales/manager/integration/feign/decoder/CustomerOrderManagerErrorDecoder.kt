package br.com.zup.realwave.sales.manager.integration.feign.decoder

import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.SalesManagerCheckoutException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.SalesManagerCheckoutUnknownException
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * Created by cleber on 6/8/17.
 */
@Component
class CustomerOrderManagerErrorDecoder(val counterService: CounterService) : ErrorDecoder {

    private val log = LogManager.getLogger(this.javaClass)

    override fun decode(methodKey: String, response: Response): Exception {
        counterService.increment("status.${response.status()}.$methodKey")

        return when (response.status()) {
            HttpStatus.BAD_REQUEST.value() -> {
                SalesManagerCheckoutException(
                    errorCode = PurchaseOrderErrorCode.PURCHASE_ORDER_CHECKOUT_ERROR,
                    response = String(response.body().asInputStream().readBytes())
                )
            }
            else -> {
                SalesManagerCheckoutUnknownException(
                    errorCode = PurchaseOrderErrorCode.PURCHASE_ORDER_CHECKOUT_ERROR,
                    response = String(response.body().asInputStream().readBytes())
                )
            }
        }

    }

}
