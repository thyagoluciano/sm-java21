package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferRepresentation
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.pcm.commonsmodel.representation.CompositionRepresentation
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrder
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrderCoupon
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderManagerApiService
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderRequestBuildException
import br.com.zup.realwave.sales.manager.integration.installationAttribute
import br.com.zup.realwave.sales.manager.integration.offerItems
import br.com.zup.realwave.sales.manager.integration.planOfferItems
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItem
import org.apache.commons.io.IOUtils
import org.junit.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Danilo Paiva on 24/06/17
 */
class CustomerOrderRequestTest {

    @Test
    fun customerOrderRequestFromAPurchaseOrderWithAPlan() {
        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("504999fd-f0c4-4eb0-967c-35493edbe493"),
                    type = "PLAN",
                    offerItems = planOfferItems()
                )
            ),
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "2f872db3-8157-40f9-90e4-a3b9c390605b",
                mapOf("iccid" to "1234")
            ),
            freight = Freight(
                type = Freight.Type("BR"),
                price = Price(
                    currency = "BRL",
                    amount = 2990,
                    scale = 2
                ),
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
                ),
                deliveryTotalTime = Freight.DeliveryTotalTime(3)
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )


        assertNotNull(customerOrderRequest.customerId)
        assertEquals(purchaseOrder.idAsString(), customerOrderRequest.externalId)
        assertNull(customerOrderRequest.payment.description)
        assertEquals("CREDIT_CARD", customerOrderRequest.payment.methods[0].method)
        assertEquals("payment-id", customerOrderRequest.payment.methods[0].methodId)

        assertEquals(1, customerOrderRequest.products.size)
        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", customerOrderRequest.products[0].productTypeId)
        assertEquals("1234", customerOrderRequest.products[0].installationAttributes["iccid"] as String)

        // Offer assertions
        assertEquals(1, customerOrderRequest.offers.size)
        assertEquals("504999fd-f0c4-4eb0-967c-35493edbe493", customerOrderRequest.offers[0].catalogOfferId)
        assertEquals("PLAN", customerOrderRequest.offers[0].catalogOfferType)
        assertEquals("DAY", customerOrderRequest.offers[0].validity.period)
        assertEquals(30, customerOrderRequest.offers[0].validity.duration)
        assertEquals(false, customerOrderRequest.offers[0].validity.unlimited)
        assertEquals("11.00 BRL", customerOrderRequest.offers[0].price.toString())
        assertEquals("plan name", customerOrderRequest.offers[0].catalogOfferName)
        assertEquals("1GB de Internet limitado e 100Minutos de ligação para qualquer operadora", customerOrderRequest.offers[0].catalogOfferDescription)

        // Offer items assertions
        assertEquals(2, customerOrderRequest.offers[0].offerItems.size)
        assertEquals(
            "990ddd1a-8ce4-4ec8-88f9-06968e972dd1",
            customerOrderRequest.offers[0].offerItems[0].catalogOfferItemId
        )
        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", customerOrderRequest.offers[0].offerItems[0].productTypeId)
        assertEquals("1.00 BRL", customerOrderRequest.offers[0].offerItems[0].price.toString())
        assertTrue(customerOrderRequest.offers[0].offerItems[0].recurrent)
        assertNotNull(customerOrderRequest.offers.first().offerItems.first().userParameters)

        assertEquals("f33078cf-ac53-4346-9a40-fd2abbabdc42", customerOrderRequest.offers[0].offerItems[0].compositionId)
        assertEquals("INTERNET 4G 1GB", customerOrderRequest.offers[0].offerItems[0].compositionName)
        assertEquals("abe6b96c-f516-4627-9354-0fd399b73e97", customerOrderRequest.offers[0].offerItems[1].compositionId)
        assertEquals("100 MINUTOS", customerOrderRequest.offers[0].offerItems[1].compositionName)


        assertEquals(
            "c68931dd-fe4e-4cc3-a8dd-ec77f3f3eeb0",
            customerOrderRequest.offers[0].offerItems[1].catalogOfferItemId
        )
        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", customerOrderRequest.offers[0].offerItems[1].productTypeId)
        assertEquals("10.00 BRL", customerOrderRequest.offers[0].offerItems[1].price.toString())
        assertTrue(customerOrderRequest.offers[0].offerItems[1].recurrent)
    }

    @Test
    fun customerOrderRequestWithSecurityCode() {
        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("504999fd-f0c4-4eb0-967c-35493edbe493"),
                    type = "PLAN",
                    offerItems = planOfferItems()
                )
            ),
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "2f872db3-8157-40f9-90e4-a3b9c390605b",
                mapOf("iccid" to "1234")
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = listOf(
                SecurityCode(
                    methodId = "payment-id",
                    securityCode = "123"
                ),
                SecurityCode(
                    methodId = "payment-id-1",
                    securityCode = "456"
                )
            )
        )


        assertEquals("123", customerOrderRequest.payment.methods.first().securityCode)
        assertEquals("456", customerOrderRequest.payment.methods.find { it.methodId == "payment-id-1" }?.securityCode)
        assertNull(customerOrderRequest.payment.methods.last().securityCode)
    }

    @Test
    fun customerOrderRequestFromAPurchaseOrderWithCustomPlan() {
        val purchaseOrderId = PurchaseOrderId()
        val items = listOf(
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("c122d069-0781-4adf-bcb8-fa37c609907b"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "1225233c-9caf-4a8f-8968-82b5e00c852d", "100 MINUTOS",
                    Price("BRL", 100, 2), true
                )
            ),
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("b65b4328-64c3-473a-ac28-eb3142a3a4bf"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "3fce9379-bf5c-4975-8f99-cb83f01c0afc", "100 MINUTOS",
                    Price("BRL", 100, 2), true
                )
            ),
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("12ccdb88-7778-4663-9d23-3e0301341a7f"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "c7a1036d-5499-42eb-b3e1-1c7901e251b7", "100 MINUTOS",
                    Price("BRL", 100, 2), true
                )
            )
        )

        val sortedItems: LinkedHashSet<Item> = linkedSetOf()
        items.forEach { sortedItems.add(it) }

        val purchaseOrder = buildPurchaseOrder(
            items = items,
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "e2ea4e38-9756-4de3-a016-64bc240ad6de",
                mapOf("iccid" to "1234")
            )
        )

        purchaseOrder.items = sortedItems

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details-with-one-custom-plan.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )

        assertNotNull(customerOrderRequest.customerId)
        assertEquals(purchaseOrder.idAsString(), customerOrderRequest.externalId)
        assertEquals("CREDIT_CARD", customerOrderRequest.payment.methods[0].method)
        assertEquals("payment-id", customerOrderRequest.payment.methods[0].methodId)
        assertEquals(2, customerOrderRequest.products.size)
        assertEquals("e2ea4e38-9756-4de3-a016-64bc240ad6de", customerOrderRequest.products[0].productTypeId)
        assertEquals("1234", customerOrderRequest.products[0].installationAttributes["iccid"] as String)

        // Offer assertions
        assertEquals(1, customerOrderRequest.offers.size)
        assertEquals("custom-plan", customerOrderRequest.offers[0].catalogOfferId)
        assertEquals("CUSTOMPLAN", customerOrderRequest.offers[0].catalogOfferType)
        assertEquals("DAY", customerOrderRequest.offers[0].validity.period)
        assertEquals(20, customerOrderRequest.offers[0].validity.duration)
        assertEquals(false, customerOrderRequest.offers[0].validity.unlimited)
        assertEquals("3.00 BRL", customerOrderRequest.offers[0].price.toString())
        assertEquals(
            "100 M + 500 MB + 200 minutos outras operadoras",
            customerOrderRequest.offers[0].catalogOfferDescription
        )

        val item1 =
            customerOrderRequest.offers[0].offerItems.first { it.catalogOfferItemId == "c7a1036d-5499-42eb-b3e1-1c7901e251b7" }

        assertEquals("84b3a1e3-311d-4a70-b4ec-f6df20859dd6", item1.compositionId)
        assertEquals("200 minutos outras operadoras", item1.compositionName)


        // Offer items assertions
        assertEquals(3, customerOrderRequest.offers[0].offerItems.size)
        assertEquals("e2ea4e38-9756-4de3-a016-64bc240ad6de", item1.productTypeId)
        assertEquals("1.00 BRL", item1.price.toString())
        assertTrue(item1.recurrent)
    }

    @Test
    fun customerOrderRequestFromAPurchaseOrderWithAddonOnly() {
        val purchaseOrderId = PurchaseOrderId()
        val items = listOf(
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("3acf6e51-3d1b-4071-a4de-9927d7ca0faa"),
                type = "ADDON",
                validity = OfferValidity("MONTH", 1, false),
                offerItems = offerItems(
                    "b15a7290-4925-4552-bf86-045296134c2a", "Internet 2GB",
                    Price("BLR", 5000000, 2), false
                )
            )
        )

        val purchaseOrder = buildPurchaseOrder(
            items = items,
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "4bd2f2c5-2a1d-4977-b50d-c858f9019a83",
                mapOf("iccid" to "1234")
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details-only-addon.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )

        assertNotNull(customerOrderRequest.customerId)
        assertEquals(purchaseOrder.idAsString(), customerOrderRequest.externalId)
        assertEquals("CREDIT_CARD", customerOrderRequest.payment.methods[0].method)
        assertEquals("payment-id", customerOrderRequest.payment.methods[0].methodId)
        assertEquals(1, customerOrderRequest.products.size)
        assertEquals("4bd2f2c5-2a1d-4977-b50d-c858f9019a83", customerOrderRequest.products[0].productTypeId)
        assertEquals("1234", customerOrderRequest.products[0].installationAttributes["iccid"] as String)

        // Offer assertions
        assertEquals(1, customerOrderRequest.offers.size)
        assertEquals("3acf6e51-3d1b-4071-a4de-9927d7ca0faa", customerOrderRequest.offers[0].catalogOfferId)
        assertEquals("ADDON", customerOrderRequest.offers[0].catalogOfferType)
        assertEquals("MONTH", customerOrderRequest.offers[0].validity.period)
        assertEquals(1, customerOrderRequest.offers[0].validity.duration)
        assertEquals(false, customerOrderRequest.offers[0].validity.unlimited)
        assertEquals("11.00 BRL", customerOrderRequest.offers[0].price.toString())
        assertEquals("addon name", customerOrderRequest.offers[0].catalogOfferName)
        assertEquals("Internet 2GB", customerOrderRequest.offers[0].catalogOfferDescription)

        val item1 =
            customerOrderRequest.offers[0].offerItems.first { it.catalogOfferItemId == "b15a7290-4925-4552-bf86-045296134c2a" }

        assertEquals("2f31c477-8da8-42ac-ae69-4d19c1bc64fc", item1.compositionId)
        assertEquals("Internet 2GB", item1.compositionName)


        // Offer items assertions
        assertEquals(1, customerOrderRequest.offers[0].offerItems.size)
        assertEquals("4bd2f2c5-2a1d-4977-b50d-c858f9019a83", item1.productTypeId)
        assertEquals("50000.00 BLR", item1.price.toString())
        assertFalse(item1.recurrent)
    }

    @Test
    fun customerOrderRequestWithPaymentDescription() {
        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("504999fd-f0c4-4eb0-967c-35493edbe493"),
                    type = "PLAN",
                    offerItems = planOfferItems()
                )
            ),
            installationAttribute = installationAttribute(
                "2f872db3-8157-40f9-90e4-a3b9c390605b",
                mapOf("iccid" to "1234")
            ),
            payment = Payment(
                methods = listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id")),
                description = Payment.Description("Payment description")
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )



        assertEquals("Payment description", customerOrderRequest.payment.description)
        assertEquals("CREDIT_CARD", customerOrderRequest.payment.methods[0].method)
        assertEquals("card-id", customerOrderRequest.payment.methods[0].methodId)

    }

    @Test(expected = CustomerOrderRequestBuildException::class)
    fun customerOrderRequestWithWrongOfferId() {
        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("504999fd-f0c4-4eb0-967c-35493edbe49"),
                    type = "PLAN",
                    offerItems = planOfferItems()
                )
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = null
        )
    }

    @Test(expected = CustomerOrderRequestBuildException::class)
    fun customerOrderRequestWithWrongOfferItemId() {
        val purchaseOrderId = PurchaseOrderId()
        val items = listOf(
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("9fb38ac3-10ee-460e-9772-358c0beaddd"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "65745bb1-ba36-4476-9e64-3201b7e2982", "100 MINUTOS",
                    Price("BLR", 100, 2), true
                )
            )
        )

        val purchaseOrder = buildPurchaseOrder(
            items = items,
            purchaseOrderId = purchaseOrderId
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = null
        )
    }

    @Test
    fun customerOrderRequestFromAPurchaseOrderWithTwoCustomPlan() {
        val purchaseOrderId = PurchaseOrderId()
        val items = listOf(
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("9fb38ac3-10ee-460e-9772-358c0beaddd2"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "65745bb1-ba36-4476-9e64-3201b7e29821", "100 MINUTOS",
                    Price("BRL", 100, 2), true
                )
            ),
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("9fb38ac3-10ee-460e-9772-358c0beaddd3"),
                type = "CUSTOMPLAN",
                validity = OfferValidity("DAY", 20, false),
                offerItems = offerItems(
                    "65745bb1-ba36-4476-9e64-3201b7e29822", "100 MINUTOS",
                    Price("BRL", 100, 2), true
                )
            )
        )

        val purchaseOrder = buildPurchaseOrder(
            items = items,
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "2f872db3-8157-40f9-90e4-a3b9c390605b",
                mapOf("iccid" to "1234")
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details-with-two-custom-plan.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            offersDetailsResponse = offersDetailsResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )

        assertNotNull(customerOrderRequest.customerId)
        assertEquals(purchaseOrder.idAsString(), customerOrderRequest.externalId)
        assertEquals("CREDIT_CARD", customerOrderRequest.payment.methods[0].method)
        assertEquals("payment-id", customerOrderRequest.payment.methods[0].methodId)
        assertEquals(1, customerOrderRequest.products.size)
        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", customerOrderRequest.products[0].productTypeId)
        assertEquals("1234", customerOrderRequest.products[0].installationAttributes["iccid"] as String)

        val offer1 = customerOrderRequest.offers.first { it.catalogOfferId == "custom-plan" }

        // Offer assertions
        assertEquals(1, customerOrderRequest.offers.size)
        assertEquals("CUSTOMPLAN", offer1.catalogOfferType)
        assertEquals("DAY", offer1.validity.period)
        assertEquals(20, offer1.validity.duration)
        assertEquals(false, offer1.validity.unlimited)
        assertEquals("2.00 BRL", offer1.price.toString())

        val item1 = offer1.offerItems.first { it.catalogOfferItemId == "65745bb1-ba36-4476-9e64-3201b7e29821" }
        val item2 = offer1.offerItems.first { it.catalogOfferItemId == "65745bb1-ba36-4476-9e64-3201b7e29822" }


        assertEquals("abe6b96c-f516-4627-9354-0fd399b73e97", item1.compositionId)
        assertEquals("100 MINUTOS", item1.compositionName)

        assertEquals("abe6b96c-f516-4627-9354-0fd399b73e97", item2.compositionId)
        assertEquals("100 MINUTOS", item2.compositionName)

        // Offer items assertions
        assertEquals(2, offer1.offerItems.size)
        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", item1.productTypeId)
        assertEquals("1.00 BRL", item1.price.toString())
        assertTrue(item1.recurrent)

        assertEquals("2f872db3-8157-40f9-90e4-a3b9c390605b", item2.productTypeId)
        assertEquals("1.00 BRL", item2.price.toString())
        assertTrue(item2.recurrent)

    }

    @Test
    fun `checkout request coupon`() {
        val purchaseOrder = buildPurchaseOrderCoupon()
        val content = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/composition.json"),
            Charset.forName("UTF-8")
        )
        val compositionResponse = content.jsonToObject(CompositionRepresentation::class.java)

        val customerOrderRequest = CustomerOrderManagerApiService.CustomerOrderRequest.of(
            purchaseOrder = purchaseOrder,
            compositionResponse = compositionResponse,
            callbackUrl = "url",
            externalModule = "sales-manager",
            securityCodes = emptyList()
        )
        val compositionFromPurchase = purchaseOrder.items.first().offerItems.first().catalogOfferItemId.value
        val compositionIdFromRequest = customerOrderRequest.offers.first().offerItems.first().compositionId

        assertEquals(compositionFromPurchase, compositionIdFromRequest)
        assertEquals(compositionResponse.id.id, compositionIdFromRequest)

    }
    
    @Test
    fun customerOrderRequestFromAPurchaseOrderWithInvalidOfferType() {
        val purchaseOrderId = PurchaseOrderId()
        val items = listOf(
            purchaseOrderItem(
                catalogOfferId = CatalogOfferId("3acf6e51-3d1b-4071-a4de-9927d7ca0faa"),
                type = "OUTRO",
                validity = OfferValidity("MONTH", 1, false),
                offerItems = offerItems(
                    "b15a7290-4925-4552-bf86-045296134c2a", "Internet 2GB",
                    Price("BLR", 5000000, 2), false
                )
            )
        )

        val purchaseOrder = buildPurchaseOrder(
            items = items,
            purchaseOrderId = purchaseOrderId,
            installationAttribute = installationAttribute(
                "4bd2f2c5-2a1d-4977-b50d-c858f9019a83",
                mapOf("iccid" to "1234")
            )
        )

        val offersResponse = IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/offers-details-only-addon.json"),
            Charset.forName("UTF-8")
        )
        val offersDetailsResponse = offersResponse.jsonToObject(OfferRepresentation::class.java)

        val exception = assertFailsWith<NotFoundException> {
            CustomerOrderManagerApiService.CustomerOrderRequest.of(
                purchaseOrder = purchaseOrder,
                offersDetailsResponse = offersDetailsResponse,
                callbackUrl = "url",
                externalModule = "sales-manager",
                securityCodes = emptyList()
            )
        }
        assertEquals("CatalogOfferType", exception.resource.resource)
        assertEquals("OUTRO", exception.resource.value)


    }
}
