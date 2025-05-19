package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.sales.manager.domain.service.CatalogManagerService
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CatalogSearchErrorDecoder
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItem
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull

class CatalogManagerServiceTest : ItegrationBaseTest() {

    @Autowired
    private lateinit var catalogManagerService: CatalogManagerService

    @Test
    fun validateOffersSuccess() {
        val items = mutableSetOf(purchaseOrderItem())
        RealwaveContextHolder.getContext().organization = "teste"
        val offerValidateRequest = catalogManagerService.validateOffers(items)
        assertNotNull(offerValidateRequest)
    }

    @Test(expected = CatalogSearchErrorDecoder.CatalogSearchValidationException::class)
    fun validateOffersError() {
        val items = mutableSetOf(purchaseOrderItem())
        RealwaveContextHolder.getContext().organization = "error"
        val offerValidateRequest = catalogManagerService.validateOffers(items)
        assertNotNull(offerValidateRequest)
    }

}
