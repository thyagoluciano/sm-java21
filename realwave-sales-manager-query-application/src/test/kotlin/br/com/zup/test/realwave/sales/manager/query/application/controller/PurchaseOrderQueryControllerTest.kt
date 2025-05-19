package br.com.zup.test.realwave.sales.manager.query.application.controller

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.EventHandler
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.sales.manager.api.response.CustomerResponse
import br.com.zup.realwave.sales.manager.api.response.Description
import br.com.zup.realwave.sales.manager.api.response.PaymentResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderStatusResponse
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated
import br.com.zup.realwave.sales.manager.infrastructure.jsonToListObject
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.query.application.filter.MultiTenantFilter
import br.com.zup.test.realwave.sales.manager.query.application.config.QueryApplicationBaseTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Created by branquinho on 06/06/17.
 */
class PurchaseOrderQueryControllerTest : QueryApplicationBaseTest() {

    companion object {
        const val ORGANIZATION = "test"
        const val APPLICATION = "59e421db1ba2798de9fe220b630a123de2609587"
        const val ORGANIZATION_DESCRIPTION = "Identify your organization"
        const val APPLICATION_DESCRIPTION = "Identify your application"
        const val STRING_TYPE = "String"
        const val PROTOCOL = "Unique protocol that was associated with Purchase Order"
        const val PURCHASE_ORDER_ID = "Id of the purchase order, it identifies it uniquely in the system"
        const val CUSTOMER_ID = "Id of a customer, such customer is correlated with a list of purchase orders"
    }

    val PURCHASE_ORDER_ID = "Id of the purchase order, it identifies it uniquely in the system"

    val CUSTOMER_ID = "Id of a customer, such customer is correlated with a list of purchase orders"

    @Rule
    @JvmField
    val restDocumentation = JUnitRestDocumentation("target/generated-snippets")

    @Autowired
    lateinit var context: WebApplicationContext

    @Autowired
    lateinit var purchaseOrderEventHandler: EventHandler

    lateinit var mockMvc: MockMvc

    lateinit var document: RestDocumentationResultHandler

    @Autowired
    lateinit var multiTenantFilter: MultiTenantFilter

    @Autowired
    lateinit var trackingFilter: FilterRegistrationBean

    private fun getMetaData(): MetaData {
        val metaData = MetaData()
        metaData[RealwaveContextConstants.ORGANIZATION_SLUG_HEADER] = RealwaveContextHolder.getContext().organization
        metaData[RealwaveContextConstants.APPLICATION_ID_HEADER] = RealwaveContextHolder.getContext().application
        return metaData
    }

    @Before
    fun setUp() {
        val context = RealwaveContextHolder.getContext()
        context.application = APPLICATION
        context.organization = ORGANIZATION
        document = document(
            "{method-name}",
            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
            Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
        )
        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters<DefaultMockMvcBuilder>(multiTenantFilter, trackingFilter.filter)
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation))
            .alwaysDo<DefaultMockMvcBuilder>(this.document).build()
    }

    private fun buildItem(aggregateId: AggregateId, catalogOfferId: CatalogOfferId): PurchaseOrderItemAdded {
        val catalogOfferItemId = UUID.randomUUID().toString()

        return PurchaseOrderItemAdded(
            aggregateId = aggregateId,
            item = Item(
                catalogOfferId = catalogOfferId,
                catalogOfferType = CatalogOfferType("ADDON"),
                price = Price(
                    currency = "BRL",
                    amount = 3799,
                    scale = 2
                ),
                validity = OfferValidity(
                    period = "DAY",
                    duration = 30,
                    unlimited = false
                ),
                offerFields = OfferFields(null),
                customFields = CustomFields(ObjectMapper().createObjectNode()),
                offerItems = listOf(
                    Item.OfferItem(
                        catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(catalogOfferItemId),
                        price = Price(
                            amount = 1000,
                            currency = "BRL",
                            scale = 2
                        ),
                        userParameters = mapOf("name" to "test")
                    )
                ),
                pricesPerPeriod = listOf(
                    PricePerPeriod(
                        totalPrice = Price(
                            amount = 1000,
                            currency = "BRL",
                            scale = 2
                        ),
                        totalDiscountPrice = Price.zero(),
                        totalPriceWithDiscount = Price(
                            amount = 1000,
                            currency = "BRL",
                            scale = 2
                        ),
                        startAt = PricePerPeriod.StartAt(1),
                        endAt = PricePerPeriod.EndAt(1),
                        items = listOf(
                            PricePerPeriod.Item(
                                compositionId = PricePerPeriod.Item.CompositionId(UUID.randomUUID().toString()),
                                itemId = PricePerPeriod.Item.ItemId(catalogOfferItemId),
                                price = Price(
                                    amount = 1000,
                                    currency = "BRL",
                                    scale = 2
                                ),
                                discountPrice = Price.zero(),
                                priceWithDiscount = Price(
                                    amount = 1000,
                                    currency = "BRL",
                                    scale = 2
                                )
                            )
                        )

                    )
                )
            )
        )
    }

    private fun buildFreight(aggregateId: AggregateId, catalogOfferId: CatalogOfferId) = PurchaseOrderFreightUpdated(
        aggregateId = aggregateId,
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

    @Test
    fun findPurchaseOrderByIdSuccess2() {
        var version = 0L
        val aggregateId = PurchaseOrderId()
        val purchaseOrderCreated = PurchaseOrderCreated(
            aggregateId = aggregateId,
            purchaseOrderType = PurchaseOrderType.JOIN,
            customer = Customer(UUID.randomUUID().toString())
        )

        val catalogOfferId  = CatalogOfferId(UUID.randomUUID().toString())

        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderCreated.aggregateId,
            event = purchaseOrderCreated,
            metaData = getMetaData(),
            version = AggregateVersion(version +1)
        )

        purchaseOrderEventHandler.handle(
            aggregateId = aggregateId,
            event = PurchaseOrderPaymentUpdated(
                aggregateId = aggregateId,
                payment = Payment(
                    methods = listOf(
                        Payment.PaymentMethod(
                            method = "method",
                            methodId = "method-id",
                            securityCodeInformed = false,
                            installments = 1,
                            customFields = null,
                            price = Price(
                                currency = "BRL",
                                amount = 2990,
                                scale = 2
                            )
                        )
                    ),
                    description = Payment.Description("description")
                )
            ),
            metaData = getMetaData(),
            version = AggregateVersion(version +2)
        )

        purchaseOrderEventHandler.handle(
            aggregateId = aggregateId,
            event = buildItem(purchaseOrderCreated.aggregateId, catalogOfferId),
            metaData = getMetaData(),
            version = AggregateVersion(version +3)
        )

        purchaseOrderEventHandler.handle(
            aggregateId = aggregateId,
            event = buildFreight(purchaseOrderCreated.aggregateId, catalogOfferId),
            metaData = getMetaData(),
            version = AggregateVersion(version +4)
        )

        val result = this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", purchaseOrderCreated.aggregateId.value)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        ).andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.id", `is`(purchaseOrderCreated.aggregateId.value)))
            .andExpect(jsonPath("$.items[0].offerItems[0].userParameters", notNullValue()))
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            ).andReturn().response.contentAsString

        val purchaseOrderResponse = result.jsonToObject(PurchaseOrderResponse::class.java)
        assertNotNull(purchaseOrderResponse.items?.first()?.offerItems?.first()?.userParameters)

    }

    @Test
    fun findPurchaseOrderByIdSuccess() {
        val createdPurchaseOrder = createdPurchaseOrder(version = 0)

        val result = this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", createdPurchaseOrder.aggregateId.value)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        ).andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.id", `is`(createdPurchaseOrder.aggregateId.value)))
            .andExpect(jsonPath("$.items[0].offerItems[0].userParameters", notNullValue()))
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            ).andReturn().response.contentAsString

        val purchaseOrderResponse = result.jsonToObject(PurchaseOrderResponse::class.java)
        assertNotNull(purchaseOrderResponse.items?.first()?.offerItems?.first()?.userParameters)
    }

    @Test
    fun findStatusPurchaseOrderByIdSuccess() {
        val createdPurchaseOrder = createdPurchaseOrder(version = 0)
        val customerId = Customer(UUID.randomUUID().toString())

        val step = listOf(
            Step(
                step = "ACTIVATION",
                status = "PENDING",
                startedAt = "2017-07-20T13:59:59+000:00",
                endedAt = "2017-07-26T13:59:59+000:00",
                processed = 0,
                total = 1
            )
        )

        val customerOrder = CustomerOrder(customerOrderId = customerId.id, status = "PENDING", steps = step)

        val channel = Channel("test-channel")
        val purchaseOrderCheckedOut = PurchaseOrderCheckedOut(
            aggregateId = createdPurchaseOrder.aggregateId,
            customerOrder = customerOrder,
            channel = channel,
            securityCodeInformed = listOf(SecurityCodeInformed("method-id", false))
        )

        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderCheckedOut.aggregateId,
            event = purchaseOrderCheckedOut,
            metaData = getMetaData(),
            version = AggregateVersion(1)
        )

        val result = this.mockMvc.perform(
            get(
                "/purchase-orders/{purchaseOrderId}/status?customerId={customerId}",
                createdPurchaseOrder.aggregateId.value,
                customerId.id
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk).andReturn().response.contentAsString
        val responseAsJson = result.jsonToObject(PurchaseOrderStatusResponse::class.java)


        assertEquals(responseAsJson.customerOrder!!.steps!!.first().step, "ACTIVATION")
        assertEquals(responseAsJson.customerOrder!!.steps!!.first().status, "PENDING")
        assertEquals(responseAsJson.customerOrder!!.steps!!.first().startedAt, "2017-07-20T13:59:59+000:00")
        assertEquals(responseAsJson.customerOrder!!.steps!!.first().endedAt, "2017-07-26T13:59:59+000:00")

        assertEquals(responseAsJson.customerOrder!!.status, "PENDING")
    }

    @Test
    fun findPurchaseOrderWithoutOrganizationInHeaders() {
        val createdPurchaseOrder = createdPurchaseOrder(version = 0)
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", createdPurchaseOrder.aggregateId.value)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.X-Organization-Slug", notNullValue()))
            .andDo(
                document(
                    "{method-name}", responseFields(
                        fieldWithPath("fields.X-Organization-Slug").description(ORGANIZATION_DESCRIPTION).type(
                            STRING_TYPE
                        )
                    )
                )
            )
    }

    @Test
    fun findPurchaseOrderWithoutApplicationIdInHeaders() {
        val createdPurchaseOrder = createdPurchaseOrder(version = 0)
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", createdPurchaseOrder.aggregateId.value)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.X-Application-Id", notNullValue()))
            .andDo(
                document(
                    "{method-name}", responseFields(
                        fieldWithPath("fields.X-Application-Id").description(APPLICATION_DESCRIPTION).type(STRING_TYPE)
                    )
                )
            )
    }

    @Test
    fun findPurchaseOrderByIdFailNotFound() {
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", UUID.randomUUID())
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun findPurchaseOrderByCustomerSuccess() {
        val customer = Customer(UUID.randomUUID().toString())

        val firstPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)
        val secondPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)

        arrayListOf(
            PurchaseOrderResponse(
                id = firstPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = null,
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            ),
            PurchaseOrderResponse(
                id = secondPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = null,
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            )
        ).objectToJson()

        val result = this.mockMvc.perform(
            get("/purchase-orders?customerId={customerId}", customer.id)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestParameters(
                        parameterWithName("customerId").description(CUSTOMER_ID)
                    )
                )
            ).andReturn().response.contentAsString

        val responseAsJson = result.jsonToListObject(PurchaseOrderResponse::class.java)

        assertNotNull(responseAsJson.first().createdAt)
        assertNotNull(responseAsJson.first().updatedAt)
    }

    @Test
    fun findPurchaseOrderByCustomerAndStatusSuccess() {
        val customer = Customer(UUID.randomUUID().toString())

        val firstPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)
        val secondPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)

        arrayListOf(
            PurchaseOrderResponse(
                id = firstPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = null,
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            ),
            PurchaseOrderResponse(
                id = secondPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = null,
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            )
        ).objectToJson()

        val status = "OPENED"
        this.mockMvc.perform(
            get("/purchase-orders?customerId={customerId}&status={status}", customer.id, status)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
    }

    @Test
    fun findPurchaseOrderByCustomerAndStatusAndBetweenDates() {
        val customer = Customer(UUID.randomUUID().toString())

        val firstPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)
        val secondPurchaseOrderCustomerUpdated = createPurchaseOrderCustomerUpdated(customer = customer, version = 0)

        arrayListOf(
            PurchaseOrderResponse(
                id = firstPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = "2017-08-29",
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            ),
            PurchaseOrderResponse(
                id = secondPurchaseOrderCustomerUpdated.aggregateId.value,
                type = null,
                segmentation = null,
                mgm = null,
                salesForce = null,
                onBoardingSale = null,
                customer = CustomerResponse(customer.id),
                coupon = null,
                discount = null,
                payment = PaymentResponse(emptyList(), null),
                status = "OPENED",
                installationAttributes = arrayListOf(),
                items = arrayListOf(),
                protocol = null,
                subscriptionId = null,
                channelCreate = null,
                channelCheckout = null,
                createdAt = "2017-07-29",
                updatedAt = null,
                callback = null,
                reason = null,
                totalPrice = null,
                freight = null
            )
        ).objectToJson()

        val status = "OPENED"
        this.mockMvc.perform(
            get(
                "/purchase-orders?customerId={customerId}&status={status}",
                customer.id,
                status,
                "2017-08-29",
                LocalDateTime.now().toString()
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
    }

    @Test
    fun findPurchaseOrderByCustomerNotFound() {
        val customer = Customer(UUID.randomUUID().toString())

        val json = listOf<PurchaseOrderResponse>().objectToJson()
        this.mockMvc.perform(
            get("/purchase-orders?customerId={customerId}", customer.id)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().string(json))
    }

    @Test
    fun findPurchaseOrderByProtocol() {
        val protocol = UUID.randomUUID().toString()
        createdPurchaseOrderWithProtocol(version = 0, protocol = protocol)
        val result = this.mockMvc.perform(
            get("/purchase-orders/{protocolId}/protocol", protocol)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("protocolId").description(PROTOCOL)
                    )
                )
            ).andReturn().response.contentAsString

        val responseAsJson = result.jsonToObject(PurchaseOrderResponse::class.java)

        assertNotNull(responseAsJson.protocol)
        assertEquals(protocol, responseAsJson.protocol)
    }

    @Test
    fun shouldNotFindPurchaseOrderByProtocol() {
        val protocol = UUID.randomUUID().toString()
        createdPurchaseOrderWithProtocol(version = 0, protocol = protocol)
        this.mockMvc.perform(
            get("/purchase-orders/{protocol}/protocol", "wrong-protocol-id")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }


    private fun createdPurchaseOrderWithProtocol(version: Long, protocol: String): PurchaseOrderCreated {
        val aggregateId = PurchaseOrderId()
        val purchaseOrderCreated = PurchaseOrderCreated(aggregateId, purchaseOrderType = PurchaseOrderType.JOIN)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderCreated.aggregateId,
            event = purchaseOrderCreated,
            metaData = getMetaData(),
            version = AggregateVersion(version)
        )

        val purchaseOrderProtocolUpdated =
            PurchaseOrderProtocolUpdated(aggregateId = purchaseOrderCreated.aggregateId, protocol = Protocol(protocol))

        purchaseOrderEventHandler.handle(
            aggregateId = aggregateId,
            event = purchaseOrderProtocolUpdated,
            metaData = getMetaData(),
            version = AggregateVersion(version + 1)
        )
        return purchaseOrderCreated
    }


    private fun createdPurchaseOrder(version: Long): PurchaseOrderCreated {
        val aggregateId = PurchaseOrderId()
        val purchaseOrderCreated = PurchaseOrderCreated(aggregateId, PurchaseOrderType.JOIN)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderCreated.aggregateId, event =
            purchaseOrderCreated, metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderItemAdded = PurchaseOrderItemAdded(
            aggregateId = aggregateId, item = Item(
                catalogOfferId = CatalogOfferId(UUID.randomUUID().toString()),
                catalogOfferType = CatalogOfferType("ADDON"),
                price = Price(
                    currency = "BRL",
                    amount = 3799,
                    scale = 2
                ),
                validity = OfferValidity(
                    period = "DAY",
                    duration = 30,
                    unlimited = false
                ),
                offerFields = OfferFields(null),
                customFields = CustomFields(ObjectMapper().createObjectNode()),
                offerItems = listOf(
                    Item.OfferItem(
                        catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(UUID.randomUUID().toString()),
                        price = Price(
                            amount = 1000,
                            currency = "BRL",
                            scale = 2
                        ),
                        userParameters = mapOf("name" to "test")
                    )
                )
            )
        )
        purchaseOrderEventHandler.handle(
            aggregateId = aggregateId,
            event = purchaseOrderItemAdded,
            metaData = getMetaData(),
            version = AggregateVersion(version + 1)
        )
        return purchaseOrderCreated
    }

    private fun createPurchaseOrderCustomerUpdated(customer: Customer, version: Long): PurchaseOrderCustomerUpdated {
        val purchaseOrderCreated = createdPurchaseOrder(version)
        val purchaseOrderCustomerUpdated = PurchaseOrderCustomerUpdated(purchaseOrderCreated.aggregateId, customer)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderCustomerUpdated.aggregateId, event =
            purchaseOrderCustomerUpdated, metaData = getMetaData(), version = AggregateVersion(version + 2)
        )
        return purchaseOrderCustomerUpdated
    }

    @Test
    fun `find payment description`() {
        val createdPurchaseOrder = createdPurchaseOrder(version = 0)

        val description = Description("The payment description")

        val purchaseOrderPaymentUpdated = PurchaseOrderPaymentUpdated(
            aggregateId = createdPurchaseOrder.aggregateId,
            payment = Payment(
                description = Payment.Description(description.value!!)
            )
        )

        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderPaymentUpdated.aggregateId,
            event = purchaseOrderPaymentUpdated,
            metaData = getMetaData(),
            version = AggregateVersion(2)
        )

        val result = this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", createdPurchaseOrder.aggregateId.value)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().isOk).andReturn().response.contentAsString
        val responseAsJson = result.jsonToObject(PurchaseOrderResponse::class.java)

        assertEquals(createdPurchaseOrder.aggregateId.value, responseAsJson.id)
        assertEquals(description, responseAsJson.payment.description)
    }

}
