package br.com.zup.realwave.sales.manager.integration.feign.decoder

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.stereotype.Component

/**
 * Created by cleber on 6/8/17.
 */
@Component
class ProductCatalogManagerErrorDecoder(val counterService: CounterService) : ErrorDecoder {

    private val log = LogManager.getLogger(this.javaClass)

    override fun decode(methodKey: String, response: Response): Exception {
        counterService.increment("status.${response.status()}.$methodKey")

        throw BusinessException.of(PurchaseOrderErrorCode.CATALOG_SEARCH_INTEGRATION_ERROR)
    }

}
