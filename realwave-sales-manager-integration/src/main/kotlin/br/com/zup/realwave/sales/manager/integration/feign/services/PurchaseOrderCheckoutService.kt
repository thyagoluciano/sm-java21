package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.cm.commonsmodel.domain.offer.OfferId
import br.com.zup.realwave.cm.commonsmodel.domain.offer.OfferType
import br.com.zup.realwave.cms.client.offer.CmsOfferClient
import br.com.zup.realwave.cms.client.offer.request.OfferGetRequest
import br.com.zup.realwave.cms.client.offer.request.OfferIdTypeRequest
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckout
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.integration.feign.FeignQualifier
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderManagerApiService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Created by cleber on 6/8/17.
 */
@Service
class PurchaseOrderCheckoutService : PurchaseOrderCheckout {

    private val log = LogManager.getLogger(this.javaClass)

    @Autowired
    private lateinit var purchaseOrderValidator: PurchaseOrderValidator

    @Autowired
    private lateinit var cmsOfferClient: CmsOfferClient

    @Autowired
    @Qualifier(FeignQualifier.CUSTOMER_ORDER_MANAGER_API)
    private lateinit var customerOrderManagerApiService: CustomerOrderManagerApiService

    @Value("\${com.callback.url}")
    private lateinit var callbackUrl: String

    @Value("\${external.module}")
    private lateinit var externalModule: String

    override fun checkout(
        purchaseOrder: PurchaseOrder,
        channel: Channel,
        securityCodes: List<SecurityCode>?
    ): CustomerOrder {
        purchaseOrderValidator.validate(purchaseOrder) // TODO validar funcao
        log.info("Begin search Offers in CMS")
        val offerRepresentation = cmsOfferClient.offers(
            OfferGetRequest(
                purchaseOrder.items.map {
                    OfferIdTypeRequest(
                        id = OfferId(it.catalogOfferId.value),
                        type = OfferType.of(it.catalogOfferType.value)
                    )
                }
            )
        )
        //TODO validation product in customerInfo
        log.info("End search Offers in CMS")

        val comCheckoutRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offerRepresentation,
            callbackUrl = callbackUrl,
            externalModule = externalModule,
            securityCodes = securityCodes
        )

        log.info("Begin create customer order type checkout")
        //TODO: for para checkout de todas purchase orders
        val customerOrder = customerOrderManagerApiService.checkoutPurchaseOrder(comCheckoutRequest).toCustomerOrder()
        log.info("End create customer order type checkout")
        return customerOrder
    }

}
