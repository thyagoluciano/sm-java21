package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrder
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrderWithBoleto
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderRequestBuildException
import br.com.zup.realwave.sales.manager.integration.offerItemWithProductId
import br.com.zup.realwave.sales.manager.integration.offerItemWithoutOneProductId
import br.com.zup.realwave.sales.manager.integration.offersItems
import br.com.zup.realwave.sales.manager.integration.pricesPerPeriod
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItem
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Created by Danilo Paiva on 24/06/17
 */
class PurchaseOrderCheckoutServiceServiceTest : ItegrationBaseTest() {


    @Autowired
    private lateinit var purchaseOrderCheckout: PurchaseOrderCheckoutService

    private val channel = Channel("test-channel")

    @Before
    fun loadTenant() {
        val context = RealwaveContextHolder.getContext()
        context.application = "PurchaseOrderCommandControllerTest"
        context.organization = "PurchaseOrderCommandControllerTest"
        context.channel = "channel"
    }

    @Test
    fun customerOrderCreatedWithTypeOfferActivation() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithProductId()
                )
            ),
            type = PurchaseOrderType.BUY
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
    }

    @Test
    fun `customer Order Created With Type Join When Payment Method is NOT BOLETO`() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offersItems(),
                    pricesPerPeriod = pricesPerPeriod()
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
        assertNull(response.boleto)
    }

    @Test
    fun `customer Order Created With Type Join and With Freight`() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offersItems(),
                    pricesPerPeriod = pricesPerPeriod()
                )
            ),
            type = PurchaseOrderType.JOIN,
            freight = Freight(
                type = Freight.Type("NOR"),
                price = Price(
                    currency = "BRL",
                    scale = 2,
                    amount = 1000
                ),
                deliveryTotalTime = Freight.DeliveryTotalTime(3),
                address = Freight.Address(
                    city = "Uberlândia",
                    complement = "7o. Andar",
                    country = "Brazil",
                    district = "Tibery",
                    name = "ZUP",
                    state = "MG",
                    street = "Av Rondon Pacheco",
                    zipCode = "38400000",
                    number = "2345"
                )
            )
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
        assertNull(response.boleto)
    }

    @Test
    fun customerOrderCreatedWithTypeChangeWithSubscriptionId() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithProductId()
                )
            ),
            type = PurchaseOrderType.CHANGE,
            subscription = "subscription-id"
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotCreateCustomerOrderWithTypeOfferActivationMissingAProduct() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithoutOneProductId()
                )
            ),
            type = PurchaseOrderType.BUY
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotCreateCustomerOrderWithTypeChangeMissingSubscriptionId() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithProductId()
                )
            ),
            type = PurchaseOrderType.CHANGE
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotCreateCustomerOrderWithTypeJoinWithProducts() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithoutOneProductId()
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
    }

    @Test(expected = CustomerOrderRequestBuildException::class)
    fun customerOrderWithoutOffersDescription() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("4c0dc07d-25ba-40bd-9a8e-ef9b552667de"),
                    offerItems = offersItems(),
                    type = "ADDON"
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
    }

    @Test(expected = CustomerOrderRequestBuildException::class)
    fun customerOrderWithoutOfferId() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("4c0dc07d-25ba-40bd-9a8e-ef9b552667df"),
                    offerItems = offersItems(),
                    type = "PLAN"
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response)
    }

    @Test
    fun `customer Order Created With Type Join When Payment Method is BOLETO`() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrderWithBoleto(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offersItems()
                )
            ),
            type = PurchaseOrderType.JOIN

        )
        val response = purchaseOrderCheckout.checkout(purchaseOrder, channel)
        assertNotNull(response.boleto)
    }

}
