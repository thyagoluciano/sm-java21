package br.com.zup.test.realwave.sales.manager.application.controller

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.Repository
import br.com.zup.eventsourcing.core.RepositoryManager
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.sales.manager.api.request.CallbackRequest
import br.com.zup.realwave.sales.manager.api.request.CheckoutRequest
import br.com.zup.realwave.sales.manager.api.request.CouponRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerOrderCallbackRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerRequest
import br.com.zup.realwave.sales.manager.api.request.FreightRequest
import br.com.zup.realwave.sales.manager.api.request.InstallationAttributesRequest
import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.MgmRequest
import br.com.zup.realwave.sales.manager.api.request.OnBoardingSaleRequest
import br.com.zup.realwave.sales.manager.api.request.PaymentRequest
import br.com.zup.realwave.sales.manager.api.request.ProtocolRequest
import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderCouponRequest
import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderRequest
import br.com.zup.realwave.sales.manager.api.request.SegmentationRequest
import br.com.zup.realwave.sales.manager.api.request.SubscriptionRequest
import br.com.zup.realwave.sales.manager.api.response.CheckoutResponse
import br.com.zup.realwave.sales.manager.api.response.CreatePurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderItemResponse
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.infrastructure.JacksonExtension
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.ErrorMessageResponse
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.jsonToListObject
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.infrastructure.toMap
import br.com.zup.realwave.sales.manager.infrastructure.valueToTree
import br.com.zup.test.realwave.sales.manager.application.config.ApplicationBaseTest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put
import org.springframework.restdocs.payload.JsonFieldType.OBJECT
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.Charset
import java.util.UUID
import kotlin.collections.set
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import br.com.zup.realwave.sales.manager.api.request.Step as StepRequest

/**
 * Created by luizs on 24/05/2017
 */
class PurchaseOrderCommandControllerTest : ApplicationBaseTest() {

    @Autowired
    lateinit var repositories: List<Repository<PurchaseOrder>>

    lateinit var repository: RepositoryManager<PurchaseOrder>

    @Before
    fun buildRepository() {
        repository = RepositoryManager(repositories)
    }

    companion object {
        const val ORGANIZATION = "PurchaseOrderCommandControllerTest"
        const val APPLICATION = "anyApplicationId"
        const val PURCHASE_ORDER_ID = "A unique identifier for the Purchase Order"
        const val PURCHASE_ORDER_DELETE = "A purchase order can only be deleted when its status is \"OPENED\""
        const val OFFER_ID =
            "A unique identifier for the offer. This information usually comes from the Catalog Manager"
        const val OFFER_TYPE =
            "A string representing the offer type (PLAN, CUSTOM_PLAN, ADDON, etc..). This information usually comes from the Catalog Manager"
        const val OFFER_CUSTOM =
            "Use this if you want to store any information related to the offer. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val OFFER_CUSTOM_FIELDS =
            "Use this if you want to store any information related to the offer. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val VALIDITY_PERIOD_FIELDS = "Period of the validity. It can be \"HOUR\", \", DAY\" or \"MONTH\""
        const val VALIDITY_DURATION_FIELDS = "Total duration of validity"
        const val VALIDITY_UNLIMITED_FIELDS = "The validity is unlimited true or false"
        const val CATALOG_OFFER_ID =
            "A unique identifier for the offer item. This information usually comes from the Catalog Manager"
        const val OFFER_ITEM_RECURRENT = "A boolean stating if this offer item is recurrent"
        const val OFFER_ITEM_CUSTOM_FIELDS =
            "Use this if you want to store any information related to the offer item. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val COUPON_CODE = "The coupon code"
        const val COUPON_CUSTOM_FIELDS =
            "Use this if you want to store any information related to the coupon. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val MGM_CODE = "\"Member get Member\" campaign code"
        const val MGM_CUSTOM_FIELDS =
            "Use this if you want to store any information related to the \"Member get Member\" code. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val INSTALLATION_ATTRIBUTES_PRODUCT_TYPE_ID =
            "A product type id for which the installation attributes will be informed"
        const val INSTALLATION_ATTRIBUTES_ATTRIBUTES = "The installation attributes. You can pass any JSON here"
        const val CUSTOMER_ID = "A unique identifier for the customer that owns the Purchase Order"
        const val ONBOARDING_ITEM_ID = "Id of the onboarding sale item in the external system"
        const val ONBOARDING_CUSTOM_FIELDS =
            "Use this if you want to store any information related to the on-boarding sale. You can pass any JSON and it will be returned when you retrieve the Purchase Order"
        const val PAYMENT_DESC = "A description to be sent to the payment gateway"
        const val PAYMENT_ASYNC = "Indicates if the transaction can be asynchronous"
        const val PAYMENT_METHOD_DESC =
            "A description of the chosen payment method for the purchase order: CREDIT_CARD, SPONSORSHIP, ETC.."
        const val PAYMENT_METHOD_ID_DESC =
            "The id of the chosen payment method. In the case of a CREDIT_CARD, this would be the credit card id returned by the Payments Manager."
        const val PAYMENT_PRICE_CURRENCY_FIELDS =
            "The price currency, mandatory when more then one payment mean informed"
        const val PAYMENT_PRICE_AMOUNT_FIELDS = "The price amount, mandatory when more then one payment mean informed"
        const val PAYMENT_PRICE_SCALE_FIELDS =
            "The price scale or how much decimal cases the amount must have, mandatory when more then one payment mean informed"
        const val INSTALLMENTS = "The number of installments chosen for the payment method"
        const val PRODUCT_ID_FIELDS =
            "If this Offer Item is relater to an existing Product of the Customer, a unique identifier of the Product must be informed. This information usually comes from the Customer Information Manager"
        const val PRICE_CURRENCY_FIELDS = "The price currency"
        const val PRICE_AMOUNT_FIELDS = "The price amount"
        const val PRICE_SCALE_FIELDS = "The price scale or how much decimal cases the amount must have"
        const val CUSTOMER_ORDER_ID =
            "A unique identifier for Customer Order that was created by the Purchase Order checkout"
        const val CUSTOMER_ORDER_STATUS = "The Customer Order status"
        const val CUSTOMER_ORDER_STEPS = "The steps the Customer Order must finish to be considered completed"
        const val PROTOCOL_FIELDS = "Unique protocol that will be associated with Purchase Order"
        const val SUBSCRIPTION_FIELDS = "Subscription Id on Subscription Manager"
        const val TYPE_FIELDS = "Purchase order type's identifier"
        const val PAYMENT_CUSTOM_FIELDS = "Custom fields to each of methods of payment"
        const val CALLBACK_URL = "Callback URL to be used when purchase orders's status change"
        const val CALLBACK_HEADERS = "The headers needed by the callback solicitor when receiving the callback"
        const val STRING_TYPE = "String"
        const val METHOD_ID = "Payment method Id"
        const val SECURITY_CODE = "Security code of the payment method id"
        const val COUPON = "Coupon to redeem some benefit"
        const val SALES_FORCE_ID = "The Origin Seller Id"
        const val SALES_FORCE_NAME = "The Origin Seller Name"
        const val BOLETO_METHOD_ID = "ID of the boleto which will be the methodId used in payments"
        const val BOLETO_PAYLOAD = "Payload with Boleto's information"
        const val COMPOSITION_ID_FIELDS = "Composition Id"
        const val START_AT_FIELDS = "when cycle price start"
        const val END_AT_FIELDS = "when cycle price end"
        const val PRICE_WITHOUT_DISCOUNT = "price without discount"
        const val DISCOUNT_PRICE = "discount price"
        const val PRICE_WITH_DISCOUNT = "price with discount"
        const val USER_PARAMETERS_ITEM = "User's parameters for item"

        const val FREIGHT_TYPE = "Freight type identifier. This information usually comes from carrier"
        const val FREIGHT_TOTAL_TIME = "Estimated delivery time"
        const val FREIGHT_ADDRESS_NAME = "Address name"
        const val FREIGHT_ADDRESS_CITY = "City"
        const val FREIGHT_ADDRESS_COMPLEMENT = "Complement"
        const val FREIGHT_ADDRESS_COUNTRY = "Country"
        const val FREIGHT_ADDRESS_DISTRICT = "District"
        const val FREIGHT_ADDRESS_STATE = "State"
        const val FREIGHT_ADDRESS_STREET = "Street"
        const val FREIGHT_ADDRESS_ZIP_CODE = "Zip code"
        const val FREIGHT_ADDRESS_NUMBER = "Number"

        const val CODE_EXCEPTION = "Error code"
        const val DETAIL_MESSAGE_METHODS_EXCEPTION = "Duplicated Methods"
        const val MESSAGE_EXCEPTION = "Error Message"

    }

    @Test
    fun createPurchaseOrderSuccessWithType() {

        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"type\": \"BUY\", \"customer\": \"customer-ID\"}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andDo(
                document(
                    "{method-name}-buy",
                    requestFields(
                        fieldWithPath("type").description(TYPE_FIELDS),
                        fieldWithPath("customer").description(CUSTOMER_ID).optional()
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )

        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"type\": \"CHANGE\", \"customer\": \"customer-ID\"}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andDo(
                document(
                    "{method-name}-change",
                    requestFields(
                        fieldWithPath("type").description(TYPE_FIELDS),
                        fieldWithPath("customer").description(CUSTOMER_ID).optional()
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )

        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"type\": \"JOIN\", \"customer\": \"customer-ID\"}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andDo(
                document(
                    "{method-name}-join",
                    requestFields(
                        fieldWithPath("type").description(TYPE_FIELDS),
                        fieldWithPath("customer").description(CUSTOMER_ID).optional()
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun createPurchaseOrderCoupon() {

        val request = PurchaseOrderCouponRequest(
            couponCode = "COUPON_CODE",
            productId = "157FBFE3-C141-42F6-9AA2-A86F2A756D18",
            customerId = "FB6D4B05-B9CC-4C68-A5E2-381B4CF36776",
            callback = CallbackRequest("http:localhost/callback", headers = ObjectMapper().createObjectNode())
        )

        this.mockMvc.perform(
            post("/purchase-orders/coupon")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(request.objectToJson())
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("couponCode").description(COUPON),
                        fieldWithPath("productId").description(PRODUCT_ID_FIELDS),
                        fieldWithPath("customerId").description(CUSTOMER_ID),
                        fieldWithPath("callback.url").description(CALLBACK_URL),
                        fieldWithPath("callback.headers").description(CALLBACK_HEADERS)
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )

    }

    @Test
    fun createPurchaseOrderCoupon_errorWhenCouponRewardTypeIsNotRESCUE_SERVICE() {

        val request = PurchaseOrderCouponRequest(
                couponCode = "VIRAFLEX20",
                productId = "12312311",
                customerId = "29d0ab55-4061-4727-b980-5653c2d3d6f9",
                callback = CallbackRequest("http:localhost/callback", headers = ObjectMapper().createObjectNode())
        )

        val result = this.mockMvc.perform(
                post("/purchase-orders/coupon")
                        .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                        .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(request.objectToJson())
        )
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value(PurchaseOrderErrorCode.COUPON_REWARD_SERVICE_MISSING.code))
                .andExpect(jsonPath("$.message").isString)
                .andReturn()

        val errorBody = result.response.contentAsString

        assertNotNull(errorBody)
    }

    @Test
    fun createPurchaseOrderSuccess() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
    }

    @Test
    fun createPurchaseOrderSuccessWithPurchaseOrderType() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"type\": \"JOIN\"}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
    }

    @Test
    fun createPurchaseOrderInvalid() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"type\": \"IJOIN\"}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun createPurchaseOrderSuccessWithCallbackWithHeaders() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(
                    "{\"callback\": " +
                        "{ \"url\" : \"http://localhost:8080/callback\"," +
                        " \"headers\" : { \"x-gw-app-key\" : " +
                        "\"C57F4B65-0C93-46F1-87B8-D47EBADFF57\"}}}"
                )
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("callback.url").description(CALLBACK_URL),
                        fieldWithPath("callback.headers").description(CALLBACK_HEADERS)
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun createPurchaseOrderSuccessWithCallback() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{\"callback\": { \"url\" : \"http://localhost:8080/callback\"}}")
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("callback.url").description(CALLBACK_URL)
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }


    @Test
    fun createPurchaseOrderWithoutOrganizationInHeaders() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.X-Organization-Slug", CoreMatchers.notNullValue()))
            .andDo(document("{method-name}"))
    }

    @Test
    fun createPurchaseOrderWithoutApplicationIdInHeaders() {
        this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.X-Application-Id", CoreMatchers.notNullValue()))
            .andDo(document("{method-name}"))
    }

    //DELETE PURCHASE ORDER
    @Test
    fun deletePurchaseOrderSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
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
    fun deletePurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun deletePurchaseOrderDeleted() {
        val purchaseOrderId = createPurchaseOrderId()
        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)

        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    "{method-name}",
                    responseFields(
                        fieldWithPath("fields.purchaseOrderId").description(PURCHASE_ORDER_DELETE).type(STRING_TYPE)
                    )
                )
            )
    }

    //UPDATE SEGMENTATION
    @Test
    fun updateSegmentationSuccess() {
        val segmentation = getSegmentationRequest()
        val purchaseOrderId = createPurchaseOrderId()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/segmentation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(segmentation)
        )
            .andExpect(status().isOk)
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
    fun updateSegmentationPurchaseOrderNotFound() {
        val queryString = "ddd=34&customer=silver"
        val segmentation = SegmentationRequest(queryString)
        val purchaseOrderId = UUID.randomUUID().toString()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/segmentation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(segmentation.objectToJson())
        )
            .andExpect(status().isNotFound)

    }

    @Test
    fun updateSegmentationWithNoBody() {
        val purchaseOrderId = createPurchaseOrderId()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/segmentation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("")
        )
            .andExpect(status().isInternalServerError)
            .andDo(document("{method-name}"))
    }

    //UPDATE ONBOARDING SALES
    @Test
    fun updateOnboardingSaleSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val onboardingSaleId = "385a1085-870d-454e-bc80-e47c51e06ccc"
        val onBoardingSaleRequest = OnBoardingSaleRequest(onboardingSaleId)
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/onboarding-sale", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(onBoardingSaleRequest.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("id").description(ONBOARDING_ITEM_ID), fieldWithPath("customFields")
                        .description(ONBOARDING_CUSTOM_FIELDS).optional()
                    ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updateOnboardingSalePurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val onboardingSaleId = "385a1085-870d-454e-bc80-e47c51e06ccc"
        val onBoardingSaleRequest = OnBoardingSaleRequest(onboardingSaleId)
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/onboarding-sale", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(onBoardingSaleRequest.objectToJson())
        )
            .andExpect(status().isNotFound)
    }

    //UPDATE MGM CODE
    @Test
    fun updateMgmCodeSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"

        val jsonNode = obj.valueToTree<JsonNode>()
        val mgm = MgmRequest(code = "100MGM", customFields = jsonNode)

        val requestJson = mgm.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/mgm", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("code").description(MGM_CODE),
                    fieldWithPath("customFields").optional().description(MGM_CUSTOM_FIELDS)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updateMgmCode_MgmNotValid() {
        val purchaseOrderId = createPurchaseOrderId()
        val mgm = MgmRequest(code = "1")

        val requestJson = mgm.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/mgm", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("MGM"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").isNotEmpty)
    }

    //DELETE MGM CODE
    @Test
    fun deleteMgmCodeSuccess() {
        val purchaseOrderId = createPurchaseOrderId()

        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}/mgm", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isOk)
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
    fun updateMgmUnprocessableWithoutMandatoryField() {
        val purchaseOrderId = createPurchaseOrderId()

        class MgmWithoutCode

        val mgm = MgmWithoutCode()

        val requestJson = mgm.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/mgm", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))

    }

    @Test
    fun updateMgmCodePurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val objectNode = ObjectMapper().createObjectNode()
        objectNode.put("param1", "value1")
        objectNode.put("param2", "value2")
        val mgm = MgmRequest(UUID.randomUUID().toString(), objectNode)

        val requestJson = mgm.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/mgm", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }

    //UPDATE CUSTOMER
    @Test
    fun updateCustomerSuccess() {
        val customer = CustomerRequest(UUID.randomUUID().toString())
        val purchaseOrderId = createPurchaseOrderId()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/customer", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customer.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("customer").description(CUSTOMER_ID)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updateCustomerPurchaseOrderNotFound() {
        val customer = CustomerRequest(UUID.randomUUID().toString())
        val purchaseOrderId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/customer", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customer.objectToJson())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun updateCustomerWithCustomerNull() {
        class CustomerRequestWithoutCustomer(val customer: String?)

        val customer = CustomerRequestWithoutCustomer(null)
        val purchaseOrderId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/customer", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customer.objectToJson())
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun updateProtocol() {
        val purchaseOrderId = createPurchaseOrderId()
        val protocol = ProtocolRequest("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")

        val requestJson = protocol.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/protocol", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("protocol").description(PROTOCOL_FIELDS)
                )
                )
            )
    }

    @Test
    fun updateSubscription() {
        val purchaseOrderId = createPurchaseOrderId(PurchaseOrderType.CHANGE)
        val subscription = SubscriptionRequest("C57F4B65-0C93-46F1-87B8-D47EBADFF57").objectToJson()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/subscription", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(subscription)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("id").description(SUBSCRIPTION_FIELDS)
                    )
                )
            )
    }

    @Test
    fun updatePurchaseOrderType() {
        val purchaseOrderId = createPurchaseOrderId()
        val purchaseOrderType = PurchaseOrderRequest(PurchaseOrderType.CHANGE.name).objectToJson()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/type", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(purchaseOrderType)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("type").description(TYPE_FIELDS)
                )
                )
            )
    }

    @Test
    fun invalidUpdatePurchaseOrderType() {
        val purchaseOrderId = createPurchaseOrderId(PurchaseOrderType.CHANGE)
        val purchaseOrderType = PurchaseOrderRequest(PurchaseOrderType.CHANGE.name).objectToJson()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/type", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(purchaseOrderType)
        )
            .andExpect(status().isBadRequest)
    }

    //POST ITEMS
    @Test
    fun `add item`() {
        val purchaseOrderId = createPurchaseOrderId()
        val itemRequestWithProductId = getItemRequestWithProductId()

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestWithProductId)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}-buy", requestFields(
                    fieldWithPath("catalogOfferId").description(OFFER_ID),
                    fieldWithPath("catalogOfferType").description(OFFER_TYPE),
                    fieldWithPath("price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("validity.period").description(VALIDITY_PERIOD_FIELDS),
                    fieldWithPath("validity.duration").description(VALIDITY_DURATION_FIELDS),
                    fieldWithPath("validity.unlimited").description(VALIDITY_UNLIMITED_FIELDS),
                    fieldWithPath("offerFields").description(OFFER_CUSTOM).optional(),
                    fieldWithPath("customFields").description(OFFER_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].productId").description(PRODUCT_ID_FIELDS),
                    fieldWithPath("offerItems.[*].catalogOfferItemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("offerItems.[*].price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("offerItems.[*].price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("offerItems.[*].price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("offerItems.[*].customFields").description(OFFER_ITEM_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].recurrent").description(OFFER_ITEM_RECURRENT).optional(),
                    fieldWithPath("offerItems.[*].userParameters").type(OBJECT).description(USER_PARAMETERS_ITEM).optional(),
                    fieldWithPath("pricesPerPeriod.[*].totalPrice").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalPriceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalDiscountPrice").description(DISCOUNT_PRICE),
                    fieldWithPath("pricesPerPeriod.[*].startAt").description(START_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].endAt").description(END_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].compositionId").description(COMPOSITION_ID_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].itemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].price").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].priceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].discountPrice").description(DISCOUNT_PRICE)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestWithProductId)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}-change", requestFields(
                    fieldWithPath("catalogOfferId").description(OFFER_ID),
                    fieldWithPath("catalogOfferType").description(OFFER_TYPE),
                    fieldWithPath("price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("validity.period").description(VALIDITY_PERIOD_FIELDS),
                    fieldWithPath("validity.duration").description(VALIDITY_DURATION_FIELDS),
                    fieldWithPath("validity.unlimited").description(VALIDITY_UNLIMITED_FIELDS),
                    fieldWithPath("offerFields").description(OFFER_CUSTOM).optional(),
                    fieldWithPath("customFields").description(OFFER_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].productId").description(PRODUCT_ID_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].catalogOfferItemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("offerItems.[*].price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("offerItems.[*].price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("offerItems.[*].price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("offerItems.[*].customFields").description(OFFER_ITEM_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].recurrent").description(OFFER_ITEM_RECURRENT).optional(),
                    fieldWithPath("offerItems.[*].userParameters").type(OBJECT).description(USER_PARAMETERS_ITEM).optional(),
                    fieldWithPath("pricesPerPeriod.[*].totalPrice").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalPriceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalDiscountPrice").description(DISCOUNT_PRICE),
                    fieldWithPath("pricesPerPeriod.[*].startAt").description(START_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].endAt").description(END_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].compositionId").description(COMPOSITION_ID_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].itemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].price").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].priceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].discountPrice").description(DISCOUNT_PRICE)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestWithProductId)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}-change", requestFields(
                    fieldWithPath("catalogOfferId").description(OFFER_ID),
                    fieldWithPath("catalogOfferType").description(OFFER_TYPE),
                    fieldWithPath("price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("validity.period").description(VALIDITY_PERIOD_FIELDS),
                    fieldWithPath("validity.duration").description(VALIDITY_DURATION_FIELDS),
                    fieldWithPath("validity.unlimited").description(VALIDITY_UNLIMITED_FIELDS),
                    fieldWithPath("offerFields").description(OFFER_CUSTOM).optional(),
                    fieldWithPath("customFields").description(OFFER_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].productId").description(PRODUCT_ID_FIELDS),
                    fieldWithPath("offerItems.[*].catalogOfferItemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("offerItems.[*].price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("offerItems.[*].price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("offerItems.[*].price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("offerItems.[*].customFields").description(OFFER_ITEM_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].recurrent").description(OFFER_ITEM_RECURRENT).optional(),
                    fieldWithPath("offerItems.[*].userParameters").type(OBJECT).description(USER_PARAMETERS_ITEM).optional(),
                    fieldWithPath("pricesPerPeriod.[*].totalPrice").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalPriceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalDiscountPrice").description(DISCOUNT_PRICE),
                    fieldWithPath("pricesPerPeriod.[*].startAt").description(START_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].endAt").description(END_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].compositionId").description(COMPOSITION_ID_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].itemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].price").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].priceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].discountPrice").description(DISCOUNT_PRICE)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )


        val itemRequestWithoutProductId = getItemRequest()

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestWithoutProductId)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}-join", requestFields(
                    fieldWithPath("catalogOfferId").description(OFFER_ID),
                    fieldWithPath("catalogOfferType").description(OFFER_TYPE),
                    fieldWithPath("price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("validity.period").description(VALIDITY_PERIOD_FIELDS),
                    fieldWithPath("validity.duration").description(VALIDITY_DURATION_FIELDS),
                    fieldWithPath("validity.unlimited").description(VALIDITY_UNLIMITED_FIELDS),
                    fieldWithPath("offerFields").description(OFFER_CUSTOM).optional(),
                    fieldWithPath("customFields").description(OFFER_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].catalogOfferItemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("offerItems.[*].price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("offerItems.[*].price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("offerItems.[*].price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("offerItems.[*].customFields").description(OFFER_ITEM_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].recurrent").description(OFFER_ITEM_RECURRENT).optional(),
                    fieldWithPath("offerItems.[*].userParameters").type(OBJECT).description(USER_PARAMETERS_ITEM).optional(),
                    fieldWithPath("pricesPerPeriod.[*].totalPrice").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalPriceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].totalDiscountPrice").description(DISCOUNT_PRICE),
                    fieldWithPath("pricesPerPeriod.[*].startAt").description(START_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].endAt").description(END_AT_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].compositionId").description(COMPOSITION_ID_FIELDS),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].itemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].price").description(PRICE_WITHOUT_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].priceWithDiscount").description(PRICE_WITH_DISCOUNT),
                    fieldWithPath("pricesPerPeriod.[*].items.[*].discountPrice").description(DISCOUNT_PRICE)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )

    }

    @Test
    fun postItemWithoutOfferFieldsSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val itemRequest = getItemRequestWithoutOfferFields()
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequest)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun postItemPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val itemRequest = getItemRequest()
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequest)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun postItemUnprocessableWithoutMandatoryField() {
        val purchaseOrderId = createPurchaseOrderId()

        class ItemWithoutItemId(val type: String)

        val itemRequest = ItemWithoutItemId(type = "CUSTOM")

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun postItemUnprocessableWithBlankParameter() {
        val purchaseOrderId = createPurchaseOrderId()

        class ItemWithoutItemId(val type: String, val offerId: String)

        val itemRequest = ItemWithoutItemId(type = "CUSTOM", offerId = "")
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun shouldNotBeValidWithUnlimitedFalse() {
        val purchaseOrderId = createPurchaseOrderId()

        val itemRequest = getItemRequest()
        val itemRequestObject = itemRequest.jsonToObject(ItemRequest::class.java)
        val newItemRequest = itemRequestObject.validity!!.copy(period = null, duration = null, unlimited = false)

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(newItemRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldBeValidWithUnlimitedTrue() {
        val purchaseOrderId = createPurchaseOrderId()

        val itemRequest = getItemRequest()
        val itemRequestObject = itemRequest.jsonToObject(ItemRequest::class.java)
        itemRequestObject.validity!!.copy(period = null, duration = null, unlimited = true)

        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/items", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestObject.objectToJson())
        )
            .andExpect(status().isOk)
    }

    @Test
    fun updateItem() {
        val purchaseOrderId = createPurchaseOrderId()
        val itemId = createPurchaseOrderItem(purchaseOrderId)
        val itemRequestToUpdate = getItemRequestToUpdate()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/items/{itemId}", purchaseOrderId, itemId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequestToUpdate)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("catalogOfferId").description(OFFER_ID),
                    fieldWithPath("catalogOfferType").description(OFFER_TYPE),
                    fieldWithPath("price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("validity.period").description(VALIDITY_PERIOD_FIELDS),
                    fieldWithPath("validity.duration").description(VALIDITY_DURATION_FIELDS),
                    fieldWithPath("validity.unlimited").description(VALIDITY_UNLIMITED_FIELDS),
                    fieldWithPath("offerFields").description(OFFER_CUSTOM).optional(),
                    fieldWithPath("customFields").description(OFFER_CUSTOM_FIELDS).optional(),
                    fieldWithPath("offerItems.[*].productId").description(PRODUCT_ID_FIELDS),
                    fieldWithPath("offerItems.[*].catalogOfferItemId").description(CATALOG_OFFER_ID),
                    fieldWithPath("offerItems.[*].price.currency").description(PRICE_CURRENCY_FIELDS),
                    fieldWithPath("offerItems.[*].price.amount").description(PRICE_AMOUNT_FIELDS),
                    fieldWithPath("offerItems.[*].price.scale").description(PRICE_SCALE_FIELDS),
                    fieldWithPath("offerItems.[*].recurrent").description(OFFER_ITEM_RECURRENT),
                    fieldWithPath("offerItems.[*].customFields").description(OFFER_ITEM_CUSTOM_FIELDS).optional()
                )
                )
            )

    }

    @Test
    fun deleteItemSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val itemId = createPurchaseOrderItem(purchaseOrderId = purchaseOrderId)
        this.mockMvc.perform(
            delete(
                "/purchase-orders/{purchaseOrderId}/items/{catalogOfferId}",
                purchaseOrderId, itemId
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", pathParameters(
                    parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID),
                    parameterWithName("catalogOfferId").description(OFFER_ID)
                )
                )
            )
    }

    @Test
    fun deleteItemFailPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val itemId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            delete(
                "/purchase-orders/{purchaseOrderId}/items/{catalogOfferId}",
                purchaseOrderId, itemId
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    "{method-name}", pathParameters(
                    parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID),
                    parameterWithName("catalogOfferId").description(OFFER_ID)
                )
                )
            )
    }

    @Test
    fun deleteItemFailItemNotFound() {
        val purchaseOrderId = createPurchaseOrderId()
        val itemId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            delete(
                "/purchase-orders/{purchaseOrderId}/items/{catalogOfferId}",
                purchaseOrderId, itemId
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    "{method-name}", pathParameters(
                    parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID),
                    parameterWithName("catalogOfferId").description(OFFER_ID)
                )
                )
            )
    }

    @Test
    fun updateFreightSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val freightRequest = FreightRequest(
            type = "BR",
            deliveryTotalTime = 3,
            price = FreightRequest.Price(
                currency = "BRL",
                amount = 2990,
                scale = 2
            ),
            address = FreightRequest.Address(
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

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/freight", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(freightRequest.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("type").description(FREIGHT_TYPE),
                        fieldWithPath("deliveryTotalTime").description(FREIGHT_TOTAL_TIME),
                        fieldWithPath("price.amount").description(PAYMENT_PRICE_AMOUNT_FIELDS),
                        fieldWithPath("price.currency").description(PAYMENT_PRICE_CURRENCY_FIELDS),
                        fieldWithPath("price.scale").description(PAYMENT_PRICE_SCALE_FIELDS),
                        fieldWithPath("address.city").description(FREIGHT_ADDRESS_CITY),
                        fieldWithPath("address.complement").type(STRING).description(FREIGHT_ADDRESS_COMPLEMENT).optional(),
                        fieldWithPath("address.country").description(FREIGHT_ADDRESS_COUNTRY),
                        fieldWithPath("address.district").description(FREIGHT_ADDRESS_DISTRICT),
                        fieldWithPath("address.name").description(FREIGHT_ADDRESS_NAME),
                        fieldWithPath("address.state").description(FREIGHT_ADDRESS_STATE),
                        fieldWithPath("address.street").description(FREIGHT_ADDRESS_STREET),
                        fieldWithPath("address.zipCode").description(FREIGHT_ADDRESS_ZIP_CODE),
                        fieldWithPath("address.number").description(FREIGHT_ADDRESS_NUMBER)
                    ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )

    }

    @Test
    fun updatePaymentSuccessWithTwoCreditCard() {
        val purchaseOrderId = createPurchaseOrderId()
        val paymentRequestList = PaymentRequest(
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-21be8fa4-a29b-410c-9f63-1d49cab63027",
                    installments = 0,
                    price = PaymentRequest.Price(amount = 500, currency = "BLR", scale = 2),
                    customFields = JacksonExtension.jacksonObjectMapper.createObjectNode()
                ),
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-" + UUID.randomUUID().toString(),
                    installments = 0,
                    price = PaymentRequest.Price(amount = 700, currency = "BLR", scale = 2),
                    customFields = JacksonExtension.jacksonObjectMapper.createObjectNode()
                )
            ), description = null
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("methods.[*].method").description(PAYMENT_METHOD_DESC),
                        fieldWithPath("methods.[*].methodId").description(PAYMENT_METHOD_ID_DESC),
                        fieldWithPath("methods.[*].price.amount").description(PAYMENT_PRICE_AMOUNT_FIELDS).optional(),
                        fieldWithPath("methods.[*].price.currency").description(PAYMENT_PRICE_CURRENCY_FIELDS).optional(),
                        fieldWithPath("methods.[*].price.scale").description(PAYMENT_PRICE_SCALE_FIELDS).optional(),
                        fieldWithPath("methods.[*].customFields").description(PAYMENT_CUSTOM_FIELDS).optional(),
                        fieldWithPath("methods.[*].installments").description(INSTALLMENTS).optional(),
                        fieldWithPath("async").description(PAYMENT_ASYNC).optional()
                    ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updatePaymentSuccessWithOneCreditCard() {
        val purchaseOrderId = createPurchaseOrderId()
        val paymentRequestList = PaymentRequest(
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-21be8fa4-a29b-410c-9f63-1d49cab63027",
                    installments = 0,
                    customFields = JacksonExtension.jacksonObjectMapper.createObjectNode()
                )
            ), description = null
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("methods.[*].method").description(PAYMENT_METHOD_DESC),
                        fieldWithPath("methods.[*].methodId").description(PAYMENT_METHOD_ID_DESC),
                        fieldWithPath("methods.[*].customFields").description(PAYMENT_CUSTOM_FIELDS).optional(),
                        fieldWithPath("methods.[*].installments").description(INSTALLMENTS).optional(),
                        fieldWithPath("async").description(PAYMENT_ASYNC).optional()
                    ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun paymentWithDescription() {
        val purchaseOrderId = createPurchaseOrderId()
        val paymentRequestList = PaymentRequest(
            description = "Payment description",
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-21be8fa4-a29b-410c-9f63-1d49cab63027",
                    installments = 0,
                    price = PaymentRequest.Price(amount = 500, currency = "BLR", scale = 2),
                    customFields = JacksonExtension.jacksonObjectMapper.createObjectNode()
                ),
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-" + UUID.randomUUID().toString(),
                    installments = 0,
                    price = PaymentRequest.Price(amount = 700, currency = "BLR", scale = 2),
                    customFields = JacksonExtension.jacksonObjectMapper.createObjectNode()
                )
            )
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("description").description(PAYMENT_DESC).optional(),
                        fieldWithPath("methods.[*].method").description(PAYMENT_METHOD_DESC),
                        fieldWithPath("methods.[*].methodId").description(PAYMENT_METHOD_ID_DESC),
                        fieldWithPath("methods.[*].price.amount").description(PAYMENT_PRICE_AMOUNT_FIELDS).optional(),
                        fieldWithPath("methods.[*].price.currency").description(PAYMENT_PRICE_CURRENCY_FIELDS).optional(),
                        fieldWithPath("methods.[*].price.scale").description(PAYMENT_PRICE_SCALE_FIELDS).optional(),
                        fieldWithPath("methods.[*].customFields").description(PAYMENT_CUSTOM_FIELDS).optional(),
                        fieldWithPath("methods.[*].installments").description(INSTALLMENTS).optional(),
                        fieldWithPath("async").description(PAYMENT_ASYNC).optional()
                    ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
        val purchaseOrder = repository.get(AggregateId(purchaseOrderId))
        assertEquals("Payment description", purchaseOrder.payment.description?.value)
    }

    @Test
    fun updatePaymentPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val paymentRequestList = PaymentRequest(
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CART",
                    methodId = "CRC-21be8fa4-a29b-410c-9f63-1d49cab63027",
                    installments = 0,
                    price = null,
                    customFields = null
                )
            ), description = null
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun updateInstallationAttributesSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()
        val installationAttributesRequest =
            InstallationAttributesRequest(productTypeId = "FIX", attributes = jsonNode.toMap())

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("productTypeId").description(INSTALLATION_ATTRIBUTES_PRODUCT_TYPE_ID),
                    fieldWithPath("attributes").description(INSTALLATION_ATTRIBUTES_ATTRIBUTES)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updateInstallationAttributesPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()
        val installationAttributesRequest =
            InstallationAttributesRequest(productTypeId = "FIX", attributes = jsonNode.toMap())

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun updateInstallationAttributesUnprocessableWithoutMandatoryField() {
        val purchaseOrderId = createPurchaseOrderId()

        class InstallationAttributesWithoutParameters

        val installationAttributesWithoutParameters = InstallationAttributesWithoutParameters()

        val requestJson = installationAttributesWithoutParameters.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun deleteInstallationAttributesSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()
        val installationAttributesRequest =
            InstallationAttributesRequest(productTypeId = "FIX", attributes = jsonNode.toMap())
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )

        this.mockMvc.perform(
            delete(
                "/purchase-orders/{purchaseOrderId}/installation-attributes/{productTypeId}",
                purchaseOrderId, installationAttributesRequest.productTypeId
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", pathParameters(
                    parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID),
                    parameterWithName("productTypeId").description(INSTALLATION_ATTRIBUTES_PRODUCT_TYPE_ID)
                )
                )
            )
    }

    @Test
    fun deleteInstallationAttributesUnprocessable() {
        val purchaseOrderId = createPurchaseOrderId()
        this.mockMvc.perform(
            delete(
                "/purchase-orders/{purchaseOrderId}/installation-attributes/{productTypeId}",
                purchaseOrderId, "FIX"
            )
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
            .andDo(
                document(
                    "{method-name}", pathParameters(
                    parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID),
                    parameterWithName("productTypeId").description(INSTALLATION_ATTRIBUTES_PRODUCT_TYPE_ID)
                )
                )
            )
    }

    @Test
    fun installationAttributesWithNoOneAttributes() {
        val purchaseOrderId = createPurchaseOrderId()
        val installationAttributesRequest = InstallationAttributesRequest(productTypeId = "FIX", attributes = null)

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun invalidInstallationAttributes() {
        val purchaseOrderId = createPurchaseOrderId()
        val obj = HashMap<String, Any>()
        val installationAttributesRequest =
            InstallationAttributesRequest(productTypeId = "FIX", attributes = obj.toMap())

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
    }

    //UPDATE COUPON
    @Test
    fun updateCouponSuccess() {
        val purchaseOrderId = createPurchaseOrderId(PurchaseOrderType.JOIN)
        createPurchaseOrderCustomer(purchaseOrderId)
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()

        val coupon = CouponRequest(code = UUID.randomUUID().toString(), customFields = jsonNode)
        val requestJson = coupon.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/coupon", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("code").description(COUPON_CODE),
                    fieldWithPath("customFields").optional().description(COUPON_CUSTOM_FIELDS)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }

    @Test
    fun updateCouponPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()

        val coupon = CouponRequest(code = UUID.randomUUID().toString(), customFields = jsonNode)
        val requestJson = coupon.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/coupon", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun updateCouponUnprocessableWithoutMandatoryField() {
        val purchaseOrderId = createPurchaseOrderId()

        class CouponWithoutCode

        val coupon = CouponWithoutCode()

        val requestJson = coupon.objectToJson()
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/coupon", purchaseOrderId)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(requestJson)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    //VALIDATION
    @Test
    fun validatePurchaseOrderSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPayment(purchaseOrderId)
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}/validation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
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
    fun validatePurchaseOrderIsNotValid() {
        val purchaseOrderId = createPurchaseOrderId()
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPayment(purchaseOrderId)
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}/validation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, "error")
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
//                .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun validatePurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}/validation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }

    //CHECKOUT
    @Test
    fun checkoutPurchaseOrderSuccess() {
        val purchaseOrderId = createPurchaseOrderId(purchaseOrderType = PurchaseOrderType.JOIN)
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPayment(purchaseOrderId)
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "TV")
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "MOBILE")

        val checkoutRequest =
            CheckoutRequest(listOf(CheckoutRequest.SecurityCode(methodId = "PAY-method-id", securityCode = "123")))

        val result = this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .content(checkoutRequest.objectToJson())
                .header(RealwaveContextConstants.CHANNEL_CONTEXT_HEADER, "default-header")
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    ),
                    requestFields(
                        fieldWithPath("paymentSecurityCodes[].methodId").description(METHOD_ID).optional(),
                        fieldWithPath("paymentSecurityCodes[].securityCode").description(SECURITY_CODE).optional()
                    )
                )
            ).andReturn().response.contentAsString


        val customerOrderId = result.jsonToObject(CheckoutResponse::class.java)

        assertNotNull(customerOrderId.customerOrder?.id)
    }

    //CHECKOUT WITH BOLETO
    @Test
    fun checkoutPurchaseOrderSuccessWithBoleto() {
        val purchaseOrderId = createPurchaseOrderId(purchaseOrderType = PurchaseOrderType.JOIN)
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPaymentBoleto(purchaseOrderId)
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "TV")
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "MOBILE")

        val result = this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.CHANNEL_CONTEXT_HEADER, "default-header")
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    ),
                    responseFields(
                        fieldWithPath("id").description(PURCHASE_ORDER_ID),
                        fieldWithPath("customerOrder.id").description(CUSTOMER_ORDER_ID),
                        fieldWithPath("customerOrder.boleto.methodId").description(BOLETO_METHOD_ID).optional(),
                        fieldWithPath("customerOrder.boleto.payload").description(BOLETO_PAYLOAD).optional()
                    )
                )
            ).andReturn().response.contentAsString

        val customerOrderId = result.jsonToObject(CheckoutResponse::class.java)

        assertNotNull(customerOrderId.customerOrder?.id)
    }

    @Test
    fun checkoutPurchaseOrderFailWithMethodsRepeated() {
        val purchaseOrderId = createPurchaseOrderId(purchaseOrderType = PurchaseOrderType.JOIN)
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPaymentRepeat(purchaseOrderId)
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "TV")
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "MOBILE")

        val stringMethods = "[PaymentMethod(method=BOLETO, methodId=null)]"

        val result = this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.CHANNEL_CONTEXT_HEADER, "default-header")
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isBadRequest)
            .andDo(
                document(
                    "{method-name}",
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    ),
                    responseFields(
                        fieldWithPath("code").description(CODE_EXCEPTION),
                        fieldWithPath("message").description(MESSAGE_EXCEPTION),
                        fieldWithPath("detailMessage").description(DETAIL_MESSAGE_METHODS_EXCEPTION)
                    )
                )
            ).andReturn().response.contentAsString

        val erro = result.jsonToObject(ErrorMessageResponse::class.java)

        assertNotNull(erro)
        assertEquals(stringMethods, erro.detailMessage)
        assertEquals(PurchaseOrderErrorCode.MORE_ONE_PAYMENT_METHOD.code, erro.code)
    }

    //CHECKOUT
    @Test
    fun `checkout payment refused`() {
        val purchaseOrderId = createPurchaseOrderId(purchaseOrderType = PurchaseOrderType.JOIN)

        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderPayment(purchaseOrderId)
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "TV")
        createPurchaseOrderInstallationAttributes(purchaseOrderId, "MOBILE")

        val result = this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .header(RealwaveContextConstants.CHANNEL_CONTEXT_HEADER, "payment-refused")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        val errorResponse = result.jsonToObject(ErrorMessageResponse::class.java)

        assertEquals(PurchaseOrderErrorCode.PURCHASE_ORDER_CHECKOUT_ERROR.code, errorResponse.code)
        assertEquals("COM-001", errorResponse.cause?.code)
        assertEquals("Payment failed", errorResponse.cause?.message)
        assertNotNull(errorResponse.cause?.customerOrderId)
    }

    @Test
    fun checkoutPurchaseOrderNotFound() {
        val purchaseOrderId = UUID.randomUUID().toString()
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun checkoutPurchaseOrderNotOpen() {
        val purchaseOrderId = createPurchaseOrderId()
        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isOk)
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    @Test
    fun checkoutWithoutCustomer() {
        val purchaseOrderId = createPurchaseOrderId()
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        createPurchaseOrderPayment(purchaseOrderId)
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.customer", CoreMatchers.notNullValue()))
            .andDo(document("{method-name}"))
    }

    @Test
    fun checkoutWithoutPayment() {
        val purchaseOrderId = createPurchaseOrderId()
        createPurchaseOrderItem(purchaseOrderId)
        createPurchaseOrderCustomer(purchaseOrderId)
        createPurchaseOrderSegmentation(purchaseOrderId)
        this.mockMvc.perform(
            post("/purchase-orders/{purchaseOrderId}/checkout", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fields.Payment", CoreMatchers.notNullValue()))
            .andDo(document("{method-name}"))
    }

    @Test
    fun callbackPurchaseOrderSuccess() {
        val purchaseOrderId = createPurchaseOrderId()
        val customerOrderRequest = CustomerOrderCallbackRequest(
            id = "customer-order-id",
            externalId = purchaseOrderId,
            status = "PENDING",
            steps = "[{\"step\": \"ServiceInstallation\", \"status\": \"PENDING\"}]".jsonToListObject(StepRequest::class.java),
            reason = null
        )

        this.mockMvc.perform(
            post("/purchase-orders/callback")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customerOrderRequest.objectToJson())
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "{method-name}",
                    requestFields(
                        fieldWithPath("id").description(CUSTOMER_ORDER_ID),
                        fieldWithPath("externalId").description(PURCHASE_ORDER_ID),
                        fieldWithPath("status").description(CUSTOMER_ORDER_STATUS),
                        fieldWithPath("steps").description(CUSTOMER_ORDER_STEPS).optional()
                    )
                )
            )
    }

    @Test
    fun callbackPurchaseOrderNotFound() {
        val customerOrderRequest = CustomerOrderCallbackRequest(
            id = "customer-order-id",
            externalId = UUID.randomUUID().toString(),
            status = "PENDING",
            steps = "[{\"step\": \"ServiceInstallation\", \"status\": \"PENDING\"}]".jsonToListObject(StepRequest::class.java),
            reason = null
        )

        this.mockMvc.perform(
            post("/purchase-orders/callback")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customerOrderRequest.objectToJson())
        )
            .andExpect(status().isNotFound)
            .andDo(document("{method-name}"))
    }

    @Test
    fun callbackPurchaseOrderIsNotValid() {
        val customerOrderRequest = CustomerOrderCallbackRequest(
            id = null,
            externalId = null,
            status = null,
            steps = null,
            reason = null
        )

        this.mockMvc.perform(
            post("/purchase-orders/callback")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customerOrderRequest.objectToJson())
        )
            .andExpect(status().isBadRequest)
            .andDo(document("{method-name}"))
    }

    //UPDATE SALES FORCE
    @Test
    fun updateSalesForce() {
        val purchaseOrderId = createPurchaseOrderId()
        val salesForce = SalesForce(id = "id", name = "name").objectToJson()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/sales-force", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(salesForce)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "{method-name}", requestFields(
                    fieldWithPath("id").optional().description(SALES_FORCE_ID),
                    fieldWithPath("name").optional().description(SALES_FORCE_NAME)
                ),
                    pathParameters(
                        parameterWithName("purchaseOrderId").description(PURCHASE_ORDER_ID)
                    )
                )
            )
    }


    //DELETE SALES FORCE
    @Test
    fun deleteSalesForce() {
        val purchaseOrderId = createPurchaseOrderId()

        this.mockMvc.perform(
            delete("/purchase-orders/{purchaseOrderId}/sales-force", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isOk)
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
    fun FindPurchaseOrder() {
        val purchaseOrderId = createPurchaseOrderId()
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andExpect(jsonPath("$.id", CoreMatchers.equalTo(purchaseOrderId)))
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
    fun FindPurchaseOrderWithErroNotFound() {
        val purchaseOrderId = "1"
        this.mockMvc.perform(
            get("/purchase-orders/{purchaseOrderId}", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.resource", CoreMatchers.equalTo("PurchaseOrderId")))
            .andExpect(jsonPath("$.value", CoreMatchers.equalTo(purchaseOrderId)))
            .andReturn()
    }

    private fun createPurchaseOrderId(purchaseOrderType: PurchaseOrderType = PurchaseOrderType.JOIN): String {

        val result = this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(PurchaseOrderRequest(type = purchaseOrderType.name).objectToJson())
        )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andReturn()

        return result.response.contentAsString.jsonToObject(CreatePurchaseOrderResponse::class.java).id
    }

    private fun createPurchaseOrderId(): String {

        val result = this.mockMvc.perform(
            post("/purchase-orders")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", CoreMatchers.notNullValue()))
            .andReturn()

        return result.response.contentAsString.jsonToObject(CreatePurchaseOrderResponse::class.java).id
    }

    private fun createPurchaseOrderItem(purchaseOrderId: String): String {
        val itemRequest = getItemRequest()
        val result = this.mockMvc.perform(
            post("/purchase-orders/$purchaseOrderId/items")
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(itemRequest)
        )
            .andExpect(status().isOk).andReturn().response.contentAsString
        return result.jsonToObject(PurchaseOrderItemResponse::class.java).itemId
    }

    private fun createPurchaseOrderSegmentation(purchaseOrderId: String) {
        val queryString = "ddd=34&customer=silver"
        val segmentation = SegmentationRequest(queryString)
        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/segmentation", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(segmentation.objectToJson())
        )
    }

    private fun createPurchaseOrderCustomer(purchaseOrderId: String) {
        val customer = CustomerRequest(UUID.randomUUID().toString())

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/customer", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(customer.objectToJson())
        )
            .andExpect(status().isOk)

    }

    private fun createPurchaseOrderPayment(purchaseOrderId: String) {
        val paymentRequestList = PaymentRequest(
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "CREDIT_CARD",
                    methodId = "CRC-21be8fa4-a29b-410c-9f63-1d49cab63027",
                    installments = 0,
                    price = null,
                    customFields = null
                )
            ), description = null
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)
    }


    private fun createPurchaseOrderPaymentBoleto(purchaseOrderId: String) {
        val paymentRequestList = PaymentRequest(
            methods = listOf(
                PaymentRequest.PaymentMethodRequest(
                    method = "BOLETO",
                    methodId = null,
                    installments = 0,
                    price = null,
                    customFields = null
                )
            ), description = null
        )

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)
    }

    private fun getItemRequest() =
        IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/addItem-PLAN.json"),
            Charset.forName("UTF-8")
        )

    private fun getItemRequestWithProductId() =
        IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/addItem-with-product-id.json"),
            Charset.forName("UTF-8")
        )

    private fun getItemRequestToUpdate() =
        IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/updateItem-PLAN.json"),
            Charset.forName("UTF-8")
        )


    private fun getItemRequestWithoutOfferFields() =
        IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/addItemWithoutOfferFields.json"),
            Charset.forName("UTF-8")
        )

    private fun getSegmentationRequest() =
        IOUtils.toString(
            this.javaClass.classLoader.getResourceAsStream("test-data/segmentation.json"),
            Charset.forName("UTF-8")
        )

    private fun createPurchaseOrderInstallationAttributes(purchaseOrderId: String, type: String) {
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = obj.valueToTree<JsonNode>()
        val installationAttributesRequest =
            InstallationAttributesRequest(productTypeId = type, attributes = jsonNode.toMap())

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/installation-attributes", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(installationAttributesRequest.objectToJson())
        )
            .andExpect(status().isOk)
    }

    private fun getPaymentMethodsRepeated(): PaymentRequest {

        val paymentRequestList =
            PaymentRequest(
                methods = listOf(
                    PaymentRequest.PaymentMethodRequest(
                        method = "BOLETO",
                        methodId = null,
                        installments = 0,
                        price = PaymentRequest.Price("10", 10, 1),
                        customFields = null
                    ),
                    PaymentRequest.PaymentMethodRequest(
                        method = "BOLETO",
                        methodId = null,
                        installments = 0,
                        price = PaymentRequest.Price("10", 10, 1),
                        customFields = null
                    )

                ), description = null
            )
        return paymentRequestList
    }

    private fun createPurchaseOrderPaymentRepeat(purchaseOrderId: String) {
        val paymentRequestList = getPaymentMethodsRepeated()

        this.mockMvc.perform(
            put("/purchase-orders/{purchaseOrderId}/payment", purchaseOrderId)
                .header(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, ORGANIZATION)
                .header(RealwaveContextConstants.APPLICATION_ID_HEADER, APPLICATION)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(paymentRequestList.objectToJson())
        )
            .andExpect(status().isOk)

    }


}
