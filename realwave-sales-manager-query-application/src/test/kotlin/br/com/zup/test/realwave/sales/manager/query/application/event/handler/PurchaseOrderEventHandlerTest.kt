package br.com.zup.test.realwave.sales.manager.query.application.event.handler

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.EventHandler
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderReasonStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderVersionException
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.test.realwave.sales.manager.query.application.config.QueryApplicationBaseTest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Created by Danilo Paiva on 01/06/17
 */
class PurchaseOrderEventHandlerTest : QueryApplicationBaseTest() {

    @Autowired
    lateinit var purchaseOrderEventHandler: EventHandler

    @Autowired
    lateinit var purchaseOrderRepository: PurchaseOrderRepository


    private val channel = Channel("test-channel")

    @Before
    fun loadTenant() {
        val context = RealwaveContextHolder.getContext()
        context.application = "59e421db1ba2798de9fe220b630a123de2609587"
        context.organization = "teste"
    }

    private fun getMetaData(): MetaData {
        val metaData = MetaData()
        metaData[RealwaveContextConstants.ORGANIZATION_SLUG_HEADER] = RealwaveContextHolder.getContext().organization
        metaData[RealwaveContextConstants.APPLICATION_ID_HEADER] = RealwaveContextHolder.getContext().application
        return metaData
    }


    @Test
    fun createPurchaseOrderTest() {
        val purchaseOrderId = PurchaseOrderId()

        createPurchaseOrder(purchaseOrderId)

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(PurchaseOrderStatus.OPENED.name, purchaseOrderSelected.status.name)
    }

    @Test
    fun updateSegmentationTest() {
        val purchaseOrderId = PurchaseOrderId()
        val segmentation = segmentation()
        val updateSegmentationEvent = PurchaseOrderSegmentationUpdated(purchaseOrderId, segmentation)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = updateSegmentationEvent,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertNotNull(purchaseOrderSelected.segmentation!!.query)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateSegmentationFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val segmentation = segmentation()
        val updateSegmentationEvent = PurchaseOrderSegmentationUpdated(purchaseOrderId, segmentation)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = updateSegmentationEvent,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun updateMgmCodeTest() {
        val purchaseOrderId = PurchaseOrderId()
        val mgm = getMgm()
        val updateMgmEvent = PurchaseOrderMgmUpdated(aggregateId = purchaseOrderId, mgm = mgm)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = updateMgmEvent,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(mgm.code, purchaseOrderSelected.mgm!!.code)
        assertEquals(mgm.customFields, purchaseOrderSelected.mgm!!.customFields)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateMgmCodeFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val mgm = getMgm()
        val updateMgmEvent = PurchaseOrderMgmUpdated(aggregateId = purchaseOrderId, mgm = mgm)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = updateMgmEvent,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun updateCustomerTest() {
        val purchaseOrderId = PurchaseOrderId()
        val customer = getCustomer()
        val event = PurchaseOrderCustomerUpdated(aggregateId = purchaseOrderId, customer = customer)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(customer, purchaseOrderSelected.customer)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateCustomerFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val customer = getCustomer()
        val event = PurchaseOrderCustomerUpdated(aggregateId = purchaseOrderId, customer = customer)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun updateCouponTest() {
        val purchaseOrderId = PurchaseOrderId()
        val coupon = getCoupon()
        val event = PurchaseOrderCouponUpdated(aggregateId = purchaseOrderId, coupon = coupon)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(coupon.code, purchaseOrderSelected.coupon!!.code)
        assertEquals(coupon.customFields, purchaseOrderSelected.coupon!!.customFields)
    }

    @Test
    fun updateCouponMoneyWithRewardsTest() {
        val purchaseOrderId = PurchaseOrderId()
        val coupon = getCouponMoneyWithRewards()
        val event = PurchaseOrderCouponUpdated(aggregateId = purchaseOrderId, coupon = coupon)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(coupon.code, purchaseOrderSelected.coupon!!.code)
        assertEquals(coupon.reward, purchaseOrderSelected.coupon!!.reward)
        assertEquals(coupon.customFields, purchaseOrderSelected.coupon!!.customFields)
    }

    @Test
    fun updateCouponPercentWithRewardsTest() {
        val purchaseOrderId = PurchaseOrderId()
        val coupon = getCouponPercentWithRewards()
        val event = PurchaseOrderCouponUpdated(aggregateId = purchaseOrderId, coupon = coupon)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(coupon.code, purchaseOrderSelected.coupon!!.code)
        assertEquals(coupon.reward, purchaseOrderSelected.coupon!!.reward)
        assertEquals(coupon.customFields, purchaseOrderSelected.coupon!!.customFields)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateCouponFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val coupon = getCoupon()
        val event = PurchaseOrderCouponUpdated(aggregateId = purchaseOrderId, coupon = coupon)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }


    @Test
    fun updatePaymentMeanTest() {
        val purchaseOrderId = PurchaseOrderId()
        val paymentMean = Payment(listOf(Payment.PaymentMethod("CREDIT_CART", "card-id")))
        val event = PurchaseOrderPaymentUpdated(aggregateId = purchaseOrderId, payment = paymentMean)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(paymentMean.methods[0].method, purchaseOrderSelected.payment.methods[0].method)
        assertEquals(paymentMean.methods[0].methodId, purchaseOrderSelected.payment.methods[0].methodId)
    }

    @Test
    fun `update payment with description`() {
        val purchaseOrderId = PurchaseOrderId()
        val paymentMean = Payment(description = Payment.Description("The payment description"))
        val event = PurchaseOrderPaymentUpdated(aggregateId = purchaseOrderId, payment = paymentMean)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId, purchaseOrderSelected.id)
        assertEquals(paymentMean.description, purchaseOrderSelected.payment.description)

    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updatePaymentMeanFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val paymentMean = Payment(listOf(Payment.PaymentMethod("CREDIT_CART", "card-id")))
        val event = PurchaseOrderPaymentUpdated(aggregateId = purchaseOrderId, payment = paymentMean)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun deletePurchaseOrderTest() {
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderDeleted(aggregateId = purchaseOrderId)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(PurchaseOrderStatus.DELETED.name, purchaseOrderSelected.status.name)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun deletePurchaseOrderFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderDeleted(aggregateId = purchaseOrderId)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun updateInstallationAttributesTest() {
        val purchaseOrderId = PurchaseOrderId()
        val installationAttributesFix = getInstallationAttributesFix()
        val event = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesFix
        )

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId])
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )

    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateInstallationAttributesFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val installationAttributesFix = getInstallationAttributesFix()
        val event = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesFix
        )

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun deleteInstallationAttributesTest() {
        val purchaseOrderId = PurchaseOrderId()
        val installationAttributesFix = getInstallationAttributesFix()
        val event1 = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesFix
        )

        val installationAttributesMobile = getInstallationAttributesMobile()
        val event2 = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesMobile
        )

        var version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event1,
            metaData = getMetaData(), version = AggregateVersion(version++)
        )
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event2,
            metaData = getMetaData(), version = AggregateVersion(version++)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId])
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId])
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )
        assertEquals(
            installationAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId]
        )

        val eventDelete = PurchaseOrderInstallationAttributesDeleted(
            aggregateId = purchaseOrderId,
            productTypeId = installationAttributesFix.productTypeId
        )
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = eventDelete,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderDeleted = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertNull(purchaseOrderDeleted.installationAttributes[installationAttributesFix.productTypeId])
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId])
        assertEquals(
            installationAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId]
        )
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun deleteInstallationAttributesFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val installationAttributesFix = getInstallationAttributesFix()
        val event1 = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesFix
        )

        val installationAttributesMobile = getInstallationAttributesMobile()
        val event2 = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = purchaseOrderId,
            installationAttribute = installationAttributesMobile
        )

        var version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event1,
            metaData = getMetaData(), version = AggregateVersion(version++)
        )
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event2,
            metaData = getMetaData(), version = AggregateVersion(version++)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId])
        assertNotNull(purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId])
        assertEquals(
            installationAttributesFix, purchaseOrderSelected.installationAttributes[installationAttributesFix
                .productTypeId]
        )
        assertEquals(
            installationAttributesMobile, purchaseOrderSelected.installationAttributes[installationAttributesMobile
                .productTypeId]
        )

        val eventDelete = PurchaseOrderInstallationAttributesDeleted(
            aggregateId = purchaseOrderId,
            productTypeId = installationAttributesFix.productTypeId
        )
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = eventDelete,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun checkOutPurchaseOrderTest() {
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderCheckedOut(
            aggregateId = purchaseOrderId,
            customerOrder = getCustomerOrder(),
            channel = channel,
            securityCodeInformed = emptyList()
        )

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(PurchaseOrderStatus.CHECKED_OUT.name, purchaseOrderSelected.status.name)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun checkOutPurchaseOrderFailVersionTest() {
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderCheckedOut(
            aggregateId = purchaseOrderId,
            customerOrder = getCustomerOrder(),
            channel = channel,
            securityCodeInformed = emptyList()
        )

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version + 5)
        )
    }

    @Test
    fun updateSalesForceTest() {
        val purchaseOrderId = PurchaseOrderId()
        val salesForce = SalesForce(id = "id", name = "name")
        val updateSalesForceEvent =
            PurchaseOrderSalesForceUpdated(aggregateId = purchaseOrderId, salesForce = salesForce)

        val version = createPurchaseOrder(purchaseOrderId)
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = updateSalesForceEvent,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(salesForce.id, purchaseOrderSelected.salesForce!!.id)
        assertEquals(salesForce.name, purchaseOrderSelected.salesForce!!.name)
    }

    private fun getMgm(): Mgm {
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = ObjectMapper().valueToTree<JsonNode>(obj)
        return Mgm(code = UUID.randomUUID().toString(), customFields = jsonNode)
    }

    private fun getCoupon(): CouponCode {
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = ObjectMapper().valueToTree<JsonNode>(obj)
        return CouponCode(code = UUID.randomUUID().toString(), customFields = jsonNode)
    }

    private fun getCouponMoneyWithRewards(): CouponCode {
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = ObjectMapper().valueToTree<JsonNode>(obj)
        return CouponCode(
            code = UUID.randomUUID().toString(),
            customFields = jsonNode,
            reward = CouponCode.Reward(
                type = "DISCOUNT_MONEY",
                discounts = listOf(
                    CouponCode.Discount(
                        segment = null,
                        discount = Price.zero(),
                        discountAsPercent = 0
                    )
                )
            )
        )
    }

    private fun getCouponPercentWithRewards(): CouponCode {
        val obj = HashMap<String, Any>()
        obj["param1"] = "value1"
        obj["param2"] = "value2"
        val jsonNode = ObjectMapper().valueToTree<JsonNode>(obj)
        return CouponCode(
            code = UUID.randomUUID().toString(),
            customFields = jsonNode,
            reward = CouponCode.Reward(
                type = "DISCOUNT_PERCENT",
                discounts = listOf(
                    CouponCode.Discount(
                        segment = null,
                        discount = Price.zero(),
                        discountAsPercent = 0
                    )
                )
            )
        )
    }

    private fun createPurchaseOrder(purchaseOrderId: PurchaseOrderId): Long {
        var version = 0L
        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = PurchaseOrderCreated(purchaseOrderId, PurchaseOrderType.JOIN),
            metaData = getMetaData(), version = AggregateVersion(version++)
        )
        return version
    }

    private fun getCustomer(): Customer {
        return Customer("276e6413-f176-4f84-b9cc-2b59ee09710e")
    }


    private fun getInstallationAttributesFix(): InstallationAttribute {
        return InstallationAttribute(productTypeId = ProductTypeId("FIX"), attributes = buildAttributes())
    }

    private fun getInstallationAttributesMobile(): InstallationAttribute {
        return InstallationAttribute(productTypeId = ProductTypeId("MOBILE"), attributes = buildAttributes())
    }

    private fun buildAttributes(): Map<String, String> {
        return mapOf(
            "param1" to "value1",
            "param2" to "value2"
        )
    }

    private fun getCustomerOrder(): CustomerOrder {
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

        return CustomerOrder(customerOrderId = UUID.randomUUID().toString(), status = "PENDING", steps = step)
    }

    @Test
    fun updatePurchaseOrderReason() {
        val purchaseOrderId = PurchaseOrderId()
        val reason = Reason("CANCELED", null)
        val event = PurchaseOrderReasonStatusUpdated(aggregateId = purchaseOrderId, reason = reason)
        val version = createPurchaseOrder(purchaseOrderId)

        purchaseOrderEventHandler.handle(
            aggregateId = purchaseOrderId, event = event,
            metaData = getMetaData(), version = AggregateVersion(version)
        )

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(reason.code, purchaseOrderSelected.reason?.code)
    }

    private fun segmentation(): Segmentation {
        val query = """
    {
   "filter":{
      "customerId":"11111111-1111-1111-1111-111111111111",
      "mergeCatalogs":true,
      "profile":{
         "type":"CLAUSE",
         "logicalOperator":"AND",
         "clauses":[
            {
               "type":"RULE",
               "content":{
                  "key":"gender",
                  "condition":"EQUAL",
                  "value":[
                     "M"
                  ]
               }
            },
            {
               "type":"RULE",
               "content":{
                  "key":"tags.tag",
                  "condition":"EQUAL",
                  "value":[
                     "CLIENT"
                  ]
               }
            }
         ]
      },
      "catalog":{
         "type":"CLAUSE",
         "logicalOperator":"OR",
         "clauses":[
            {
               "type":"RULE",
               "content":{
                  "key":"channels.name",
                  "condition":"EQUAL",
                  "value":[
                     "CHANNEL1",
                     "AAA"
                  ]
               }
            },
            {
               "type":"RULE",
               "content":{
                  "key":"channels.name",
                  "condition":"EQUAL",
                  "value":[
                     "CHANNEL2",
                     "AAA"
                  ]
               }
            }
         ]
      }
   }
}
    """
        return Segmentation(query = query.jsonToObject(JsonNode::class.java))
    }
}
