package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.pcm.client.domain.PcmCompositionClient
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckout
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.integration.feign.FeignQualifier
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderManagerApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCouponCheckoutService : PurchaseOrderCheckout {

    @Autowired
    private lateinit var pcmClient: PcmCompositionClient

    @Autowired
    private lateinit var purchaseOrderValidator: PurchaseOrderValidator

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
    ): CustomerOrder? {

        purchaseOrderValidator.validate(purchaseOrder)

        val compositionResponse =
            pcmClient.findOne(purchaseOrder.items.first().offerItems.first().catalogOfferItemId.value, "open")

        val comCheckoutRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            compositionResponse = compositionResponse,
            callbackUrl = callbackUrl,
            externalModule = externalModule,
            securityCodes = securityCodes
        )

        //TODO: for para checkout de todas purchase orders
        return customerOrderManagerApiService.checkoutPurchaseOrder(comCheckoutRequest).toCustomerOrder()

    }

}
