package br.com.zup.realwave.sales.manager.integration.feign.decoder

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.lang.Exception

/**
 * Created by cleber on 6/7/17.
 */
@Component
class EventStoreErrorDecoder(val counterService: CounterService) : ErrorDecoder {

    override fun decode(methodKey: String, response: Response): Exception {
        counterService.increment("status.${response.status()}.$methodKey")

        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            throw NotFoundException()
        }

        throw BusinessException.of("EVENT_STORE", PurchaseOrderErrorCode.EVENT_STORE_INTEGRATION_ERROR)
    }

}
