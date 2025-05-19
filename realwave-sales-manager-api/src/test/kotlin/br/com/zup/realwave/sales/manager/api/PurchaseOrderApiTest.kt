package br.com.zup.realwave.sales.manager.api

import br.com.zup.realwave.sales.manager.api.request.CheckoutRequest
import br.com.zup.realwave.sales.manager.api.request.CouponRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerOrderCallbackRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerRequest
import br.com.zup.realwave.sales.manager.api.request.InstallationAttributesRequest
import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.MgmRequest
import br.com.zup.realwave.sales.manager.api.request.OnBoardingSaleRequest
import br.com.zup.realwave.sales.manager.api.request.PaymentRequest
import br.com.zup.realwave.sales.manager.api.request.ProtocolRequest
import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderRequest
import br.com.zup.realwave.sales.manager.api.request.SalesForceRequest
import br.com.zup.realwave.sales.manager.api.request.SubscriptionRequest
import br.com.zup.realwave.sales.manager.api.response.CheckoutResponse
import br.com.zup.realwave.sales.manager.api.response.CreatePurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.DeleteInstallationAttributesResponse
import br.com.zup.realwave.sales.manager.api.response.DeleteResponse
import br.com.zup.realwave.sales.manager.api.response.ProtocolResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderItemResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderMgmResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderSalesForceResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderStatusResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderTypeResponse
import br.com.zup.realwave.sales.manager.api.response.SegmentationResponse
import br.com.zup.realwave.sales.manager.api.response.SubscriptionResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateCouponResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateCustomerIdResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateInstallationAttributesResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateOnBoardingSaleResponse
import br.com.zup.realwave.sales.manager.api.response.UpdatePaymentResponse
import br.com.zup.realwave.sales.manager.api.response.ValidateResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import io.github.azagniotov.stubby4j.client.StubbyClient
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.cloud.netflix.feign.support.SpringMvcContract
import kotlin.test.assertNotNull

class PurchaseOrderApiTest {

    lateinit var purchaseOrderCommandApi: PurchaseOrderCommandApi

    lateinit var purchaseOrderQueryApi: PurchaseOrderQueryApi

    lateinit var jacksonObjectMapper: ObjectMapper

    private var stubbyPort: Int = 7777

    private var stubbyUrl: String = "http://localhost:"

    private var stubbyFile: String = "src/test/resources/stubby4j/api-test.yml"


    companion object {

        @BeforeClass
        @JvmStatic
        fun setUp() {
            StubbyClient().startJetty(PurchaseOrderApiTest().stubbyPort, PurchaseOrderApiTest().stubbyFile)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            StubbyClient().stopJetty()
        }

    }

    @Before
    fun buildApi() {

        jacksonObjectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(KotlinModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

        purchaseOrderCommandApi = Feign.builder()
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(jacksonObjectMapper))
            .decoder(JacksonDecoder(jacksonObjectMapper))
            .client(OkHttpClient())
            .target(PurchaseOrderCommandApi::class.java, stubbyUrl + stubbyPort)

        purchaseOrderQueryApi = Feign.builder()
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(jacksonObjectMapper))
            .decoder(JacksonDecoder(jacksonObjectMapper))
            .client(OkHttpClient())
            .target(PurchaseOrderQueryApi::class.java, stubbyUrl + stubbyPort)
    }

    @Test
    fun create() {

        val createPurchaseOrderResponse: CreatePurchaseOrderResponse = purchaseOrderCommandApi.create(
            PurchaseOrderRequest(type = null, callback = null)
        )

        assertNotNull(createPurchaseOrderResponse)

    }

    @Test
    fun createWhithCustomerId() {

        val createPurchaseOrderResponse: CreatePurchaseOrderResponse = purchaseOrderCommandApi.create(
            PurchaseOrderRequest(type = null, customer = "customerId", callback = null)
        )

        assertNotNull(createPurchaseOrderResponse)

    }

    @Test
    fun segmentation() {

        val segmentationResponse: SegmentationResponse = purchaseOrderCommandApi.segmentation(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            segmentation = ObjectMapper().createObjectNode()
        )

        assertNotNull(segmentationResponse)

    }

    @Test
    fun updateOnBoardingSale() {

        val updateOnBoardingSaleResponse: UpdateOnBoardingSaleResponse = purchaseOrderCommandApi.updateOnBoardingSale(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            onBoardingSale = OnBoardingSaleRequest(
                id = null,
                customFields = ObjectMapper().createObjectNode()
            )
        )

        assertNotNull(updateOnBoardingSaleResponse)

    }

    @Test
    fun updateMgm() {

        val updateMgmResponse: PurchaseOrderMgmResponse = purchaseOrderCommandApi.updateMgm(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            mgmRequest = MgmRequest(
                code = null,
                customFields = ObjectMapper().createObjectNode()
            )
        )

        assertNotNull(updateMgmResponse)
    }

    @Test
    fun deleteMgm() {

        purchaseOrderCommandApi.updateMgm(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            mgmRequest = MgmRequest(
                code = null,
                customFields = ObjectMapper().createObjectNode()
            )
        )

        val deleteMgm: PurchaseOrderMgmResponse =
            purchaseOrderCommandApi.deleteMgm("e9faefbe-0581-4324-a6d8-6f4727d84007")

        assertNotNull(deleteMgm)
    }

    @Test
    fun updateCustomerId() {

        val updateCustomerIdResponse: UpdateCustomerIdResponse = purchaseOrderCommandApi.updateCustomerId(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            customerRequest = CustomerRequest(null)
        )

        assertNotNull(updateCustomerIdResponse)

    }

    @Test
    fun addItem() {

        val addOrderItemResponse: PurchaseOrderItemResponse = purchaseOrderCommandApi.addItem(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            itemRequest = ItemRequest(null, null, null, null, null, null, null, null)
        )

        assertNotNull(addOrderItemResponse)

    }

    @Test
    fun updateItem() {

        val addOrderItemResponse: PurchaseOrderItemResponse = purchaseOrderCommandApi.updateItem(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            itemId = "itemId",
            itemRequest = ItemRequest(null, null, null, null, null, null, null, null)
        )

        assertNotNull(addOrderItemResponse)

    }

    @Test
    fun deleteItem() {

        val deleteOrderItemResponse: PurchaseOrderItemResponse = purchaseOrderCommandApi.deleteItem(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            itemId = "itemId"
        )

        assertNotNull(deleteOrderItemResponse)

    }

    @Test
    fun updatePayment() {

        val updatePaymentResponse: UpdatePaymentResponse = purchaseOrderCommandApi.updatePayment(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            paymentRequest = PaymentRequest(
                methods = listOf(),
                description = null
            )
        )

        assertNotNull(updatePaymentResponse)

    }

    @Test
    fun updateInstallationAttributes() {

        val updateInstallationAttributesResponse: UpdateInstallationAttributesResponse =
            purchaseOrderCommandApi.updateInstallationAttributes(
                purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
                request = InstallationAttributesRequest(
                    productTypeId = null,
                    attributes = null
                )
            )

        assertNotNull(updateInstallationAttributesResponse)

    }

    @Test
    fun deleteInstallationAttributes() {

        val deleteInstallationAttributesResponse: DeleteInstallationAttributesResponse =
            purchaseOrderCommandApi.deleteInstallationAttributes(
                purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
                productTypeId = "productTypeId"
            )

        assertNotNull(deleteInstallationAttributesResponse)

    }

    @Test
    fun updateCoupon() {

        val updateCouponResponse: UpdateCouponResponse = purchaseOrderCommandApi.updateCoupon(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            couponRequest = CouponRequest(
                code = null,
                customFields = ObjectMapper().createObjectNode()
            )
        )

        assertNotNull(updateCouponResponse)

    }

    @Test
    fun validate() {

        val validateResponse: ValidateResponse = purchaseOrderCommandApi.validate(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007"
        )

        assertNotNull(validateResponse)

    }

    @Test
    fun checkout() {

        val checkoutResponse: CheckoutResponse = purchaseOrderCommandApi.checkout(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            checkoutRequest = CheckoutRequest(listOf())
        )

        assertNotNull(checkoutResponse)

    }

    @Test
    fun delete() {

        val deleteResponse: DeleteResponse = purchaseOrderCommandApi.delete(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007"
        )

        assertNotNull(deleteResponse)

    }

    @Test
    fun callback() {

        purchaseOrderCommandApi.callback(
            customerOrderCallbackRequest = CustomerOrderCallbackRequest(
                id = null,
                status = null,
                externalId = null,
                steps = null,
                reason = null
            )
        )


    }

    @Test
    fun protocol() {

        val protocolResponse: ProtocolResponse = purchaseOrderCommandApi.protocol(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            protocolRequest = ProtocolRequest(
                protocol = "protocol"
            )
        )

        assertNotNull(protocolResponse)

    }

    @Test
    fun subscription() {

        val subscription: SubscriptionResponse = purchaseOrderCommandApi.subscription(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            subscriptionRequest = SubscriptionRequest(
                id = "id"
            )
        )

        assertNotNull(subscription)

    }

    @Test
    fun purchaseOrderType() {

        val purchaseOrdertypeResponse: PurchaseOrderTypeResponse = purchaseOrderCommandApi.purchaseOrderType(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            purchaseOrderRequest = PurchaseOrderRequest(
                type = null,
                callback = null
            )
        )

        assertNotNull(purchaseOrdertypeResponse)

    }

    @Test
    fun findByPurchaseOrder() {

        val purchaseOrderResponse: PurchaseOrderResponse? = purchaseOrderQueryApi.findByPurchaseOrderId("1")

        assertNotNull(purchaseOrderResponse)

    }

    @Test
    fun findByProtocol() {

        val purchaseOrderResponse: PurchaseOrderResponse? = purchaseOrderQueryApi.findByProtocol("1")

        assertNotNull(purchaseOrderResponse)

    }

    @Test
    fun getStatusByPurchaseOrder() {

        val purchaseOrderStatusResponse: PurchaseOrderStatusResponse? =
            purchaseOrderQueryApi.getPurchaseOrderStatus("1")

        assertNotNull(purchaseOrderStatusResponse)

    }

    @Test
    fun findByCustomer() {

        val purchaseOrderResponseList: List<PurchaseOrderResponse>? =
            purchaseOrderQueryApi.findByCustomer("ascascasc", null, null, null)

        assertNotNull(purchaseOrderResponseList)

    }

    @Test
    fun updateSalesForce() {

        val updateSalesForce: PurchaseOrderSalesForceResponse = purchaseOrderCommandApi.updateSalesForce(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            salesForceRequest = SalesForceRequest(
                id = "id",
                name = "name"
            )
        )

        assertNotNull(updateSalesForce)
    }

    @Test
    fun deleteSalesForce() {

        purchaseOrderCommandApi.updateSalesForce(
            purchaseOrderId = "e9faefbe-0581-4324-a6d8-6f4727d84007",
            salesForceRequest = SalesForceRequest(
                id = "id",
                name = "name"
            )
        )

        val deleteSalesForce: PurchaseOrderSalesForceResponse =
            purchaseOrderCommandApi.deleteSalesForce("e9faefbe-0581-4324-a6d8-6f4727d84007")

        assertNotNull(deleteSalesForce)
    }

}
