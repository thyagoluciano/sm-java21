package br.com.zup.test.realwave.sales.manager.query.event.handler

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.sales.manager.domain.Boleto
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderApplicableEvent
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerOrderUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderOnBoardingSaleUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderReasonStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderTypeUpdated
import br.com.zup.realwave.sales.manager.domain.repository.CustomerOrderRepository
import br.com.zup.realwave.sales.manager.domain.repository.FreightRepository
import br.com.zup.realwave.sales.manager.domain.repository.PaymentRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.infrastructure.common.liquibase.LiquibaseHandler
import br.com.zup.realwave.sales.manager.infrastructure.toJsonNode
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderEventHandlerImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.boot.actuate.metrics.CounterService
import java.util.UUID

/**
 * Created by luizs on 05/06/2017
 */
class EventHandlerDiscoveryTest {

    val liquibaseHandler = mock(LiquibaseHandler::class.java)
    val purchaseOrderRepository = mock(PurchaseOrderRepository::class.java)
    val customerOrderRepository = mock(CustomerOrderRepository::class.java)
    val paymentRepository = mock(PaymentRepository::class.java)
    val freightRepository = mock(FreightRepository::class.java)
    val discoveryService = buildEventHandlerDiscoveryService(
        purchaseOrderRepository,
        customerOrderRepository,
        paymentRepository,
        freightRepository
    )
    val counterService = mock(CounterService::class.java)
    val handler = PurchaseOrderEventHandlerImpl(
        liquibaseHandler = liquibaseHandler,
        counterService = counterService,
        eventHandlerDiscoveryService = discoveryService
    )

    private fun metaData(): MetaData {
        val metaData = MetaData()
        metaData[RealwaveContextConstants.ORGANIZATION_SLUG_HEADER] = "org"
        metaData[RealwaveContextConstants.APPLICATION_ID_HEADER] = "appId"
        metaData[RealwaveContextConstants.CHANNEL_CONTEXT_HEADER] = "APP"
        return metaData
    }

    @Test
    fun handlePurchaseOrderCreated() {
        val version = 0L
        val event = PurchaseOrderCreated(
            PurchaseOrderId(),
            null,
            Callback("http://localhost:8080/callback", null)
        )
        handler.handle(event.aggregateId, event, metaData(), AggregateVersion(version))

        verify(purchaseOrderRepository).savePurchaseOrder(
            purchaseOrderId = PurchaseOrderId(event.aggregateId.value),
            type = null,
            channelCreate = Channel("APP"),
            callback = event.callback,
            customer = null,
            version = version
        )
    }

    @Test
    fun handleAddPurchaseOrderItem() {
        val version = 0L
        val event = PurchaseOrderItemAdded(PurchaseOrderId(), getItem())
        handler.handle(
            aggregateId = event.aggregateId, event = event, metaData = metaData(), version =
            AggregateVersion(version)
        )

        verify(purchaseOrderRepository).addItem(event.aggregateId, event.item, version)
    }

    @Test
    fun handleDeletePurchaseOrderItem() {
        val version = 0L
        val event = PurchaseOrderItemRemoved(PurchaseOrderId(), Item.Id("offer-id"))

        handler.handle(
            aggregateId = event.aggregateId, event = event, metaData = metaData(), version =
            AggregateVersion(version)
        )

        verify(purchaseOrderRepository).removeItem(event.aggregateId, event.itemId, version)
    }

    @Test
    fun handleUpdatePurchaseOrderProtocol() {
        val version = 0L
        val protocol = Protocol("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val event = PurchaseOrderProtocolUpdated(aggregateId = PurchaseOrderId(), protocol = protocol)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateProtocol(event.aggregateId, protocol, version)
    }

    @Test
    fun handleUpdateMGM() {
        val version = 0L
        val mgm = Mgm("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val event = PurchaseOrderMgmUpdated(aggregateId = PurchaseOrderId(), mgm = mgm)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateMgm(PurchaseOrderId(event.aggregateId.value), mgm, version)

        val version1 = 1L
        val event1 = PurchaseOrderMgmDeleted(aggregateId = PurchaseOrderId(), mgm = null)

        handler.handle(
            aggregateId = event1.aggregateId,
            event = event1,
            metaData = metaData(),
            version = AggregateVersion(version1)
        )
        verify(purchaseOrderRepository).updateMgm(PurchaseOrderId(event1.aggregateId.value), null, version1)
    }

    @Test
    fun handleUpdatePurchaseOrderSubscription() {
        val version = 0L
        val subscription = Subscription("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val event = PurchaseOrderSubscriptionUpdated(PurchaseOrderId(), subscription)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateSubscription(event.aggregateId, subscription, version)
    }

    @Test
    fun handleUpdatePurchaseOrderType() {
        val version = 0L
        val type = PurchaseOrderType.CHANGE
        val event = PurchaseOrderTypeUpdated(PurchaseOrderId(), type)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updatePurchaseOrderType(event.aggregateId, type, version)
    }

    @Test
    fun handleUpdatePurchaseOrderItem() {
        val version = 0L
        val event = PurchaseOrderItemUpdated(PurchaseOrderId(), item = getItem())

        handler.handle(
            aggregateId = event.aggregateId, event = event, metaData = metaData(), version =
            AggregateVersion(version)
        )

        verify(purchaseOrderRepository).updateItem(event.aggregateId, event.item, version)
    }

    @Test
    fun handleUpdatePurchaseOrderStatus() {
        val version = 0L
        val status = PurchaseOrderStatus.CANCELED
        val event = PurchaseOrderStatusUpdated(aggregateId = PurchaseOrderId(), status = status)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateStatus(event.aggregateId, status, version)
    }

    @Test
    fun handleCheckoutPurchaseOrder() {
        val version = 0L
        val event = PurchaseOrderCheckedOut(
            aggregateId = PurchaseOrderId(),
            customerOrder = getCustomerOrder(),
            channel = Channel("channel-id"),
            securityCodeInformed = emptyList()
        )

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )

        verify(purchaseOrderRepository).checkOutPurchaseOrder(
            event.aggregateId,
            event.channel,
            version,
            event.securityCodeInformed
        )

        verify(customerOrderRepository).saveCustomerOrder(event.aggregateId, event.customerOrder!!)
    }

    @Test
    fun `handle Checkout PurchaseOrder With Boleto`() {
        val version = 0
        val event = PurchaseOrderCheckedOut(
            aggregateId = PurchaseOrderId(),
            customerOrder = getCustomerOrderWithBoleto(),
            channel = Channel("channel-id"),
            securityCodeInformed = emptyList()
        )

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version.toLong())
        )

        verify(purchaseOrderRepository).checkOutPurchaseOrder(
            event.aggregateId,
            event.channel,
            version.toLong(),
            event.securityCodeInformed
        )

        verify(customerOrderRepository).saveCustomerOrder(event.aggregateId, event.customerOrder!!)

        verify(paymentRepository).updateMethodId(event.aggregateId,event.customerOrder?.boleto!!.methodId)
    }

    @Test
    fun handleUpdatePurchaseOrderReasonStatus() {
        val version = 0L
        val reason = Reason("CANCELED", null)
        val event = PurchaseOrderReasonStatusUpdated(PurchaseOrderId(), reason)

        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updatePurchaseOrderReason(event.aggregateId, reason, version)
    }

    @Test
    fun handlePurchaseOrderCouponUpdated() {
        val version = 0L
        val coupon = CouponCode("code")
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderCouponUpdated(purchaseOrderId, coupon)
        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateCoupon(purchaseOrderId, event.coupon, version)
    }

    @Test
    fun handlePurchaseOrderCustomerOrderUpdated() {
        val version = 0L
        val event = PurchaseOrderCustomerOrderUpdated(PurchaseOrderId(), getCustomerOrder())
        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(customerOrderRepository).saveCustomerOrder(event.aggregateId, event.customerOrder)
    }

    @Test
    fun handlePurchaseOrderCustomerUpdated() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderCustomerUpdated(purchaseOrderId, Customer("customer-id"))
        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateCustomer(purchaseOrderId, event.customer, version)
    }

    @Test
    fun handlePurchaseOrderDeleted() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderDeleted(purchaseOrderId)
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).deletePurchaseOrder(purchaseOrderId, version)
    }

    @Test
    fun handlePurchaseOrderInstallationAttributesUpdated() {
        val version = 0L
        val installationAttribute = InstallationAttribute(
            productTypeId = ProductTypeId("product-type-id"),
            attributes = mapOf("aaa" to "bbb")
        )
        val event = PurchaseOrderInstallationAttributesUpdated(
            aggregateId = PurchaseOrderId(),
            installationAttribute = installationAttribute
        )
        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateInstallationAttributes(
            event.aggregateId.value,
            installationAttribute,
            version
        )
    }

    @Test
    fun handlePurchaseOrderInstallationAttributesDeleted() {
        val version = 0L
        val productTypeId = ProductTypeId("product-type-id")
        val event = PurchaseOrderInstallationAttributesDeleted(
            aggregateId = PurchaseOrderId(),
            productTypeId = productTypeId
        )
        handler.handle(
            aggregateId = event.aggregateId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).deleteInstallationAttributes(event.aggregateId.value, productTypeId, version)
    }

    @Test
    fun handlePurchaseOrderOnBoardingSaleUpdated() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val onBoardingSale = OnBoardingSale(
            offer = CatalogOfferId("catalog-offer-id"),
            customFields = null
        )
        val event = PurchaseOrderOnBoardingSaleUpdated(
            aggregateId = purchaseOrderId,
            onBoardingSale = onBoardingSale
        )
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateOnBoardingSale(purchaseOrderId, onBoardingSale, version)
    }

    @Test
    fun handlePurchaseOrderPaymentUpdated() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderPaymentUpdated(
            aggregateId = purchaseOrderId,
            payment = Payment(
                methods = listOf(Payment.PaymentMethod(method = "method", methodId = "method-id")),
                description = Payment.Description("description")
            )
        )
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updatePayment(purchaseOrderId, event.payment, version)
    }

    @Test
    fun handlePurchaseOrderFreightUpdated() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderFreightUpdated(
            aggregateId = purchaseOrderId,
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

        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )

        verify(purchaseOrderRepository).updateFreight(purchaseOrderId, event.freight, version)
    }

    @Test
    fun handlePurchaseOrderSalesForceRemoved() {
        val version = 0L
        val salesForce = null
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderSalesForceRemoved(
            aggregateId = purchaseOrderId,
            salesForce = salesForce
        )
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateSalesForce(purchaseOrderId, null, version)
    }

    @Test
    fun handlePurchaseOrderSalesForceUpdated() {
        val version = 0L
        val salesForce = SalesForce("sales-force-id", "sales-force-name")
        val purchaseOrderId = PurchaseOrderId()
        val event = PurchaseOrderSalesForceUpdated(
            aggregateId = purchaseOrderId,
            salesForce = salesForce
        )
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateSalesForce(purchaseOrderId, salesForce, version)
    }

    @Test
    fun handlePurchaseOrderSegmentationUpdated() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        val segmentation = Segmentation(mapOf("a" to "b").toJsonNode())
        val event = PurchaseOrderSegmentationUpdated(
            aggregateId = purchaseOrderId,
            segmentation = segmentation
        )
        handler.handle(
            aggregateId = purchaseOrderId,
            event = event,
            metaData = metaData(),
            version = AggregateVersion(version)
        )
        verify(purchaseOrderRepository).updateSegmentation(purchaseOrderId, segmentation, version)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidHandled() {
        val version = 0L
        val purchaseOrderId = PurchaseOrderId()
        handler.handle(
            aggregateId = purchaseOrderId,
            event = InvalidPurchaseOrderEvent(),
            metaData = metaData(),
            version = AggregateVersion(version)
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

        return CustomerOrder(customerOrderId = "customerOrderId", status = "PENDING", steps = step)
    }

    private fun getCustomerOrderWithBoleto(): CustomerOrder {
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

        val boleto = Boleto(
            methodId = "BOL-6166f38a-4c6b-4200-a6cb-37cad92b4376",
            payload = ObjectMapper().createObjectNode()
        )

        return CustomerOrder(customerOrderId = "customerOrderId", status = "PENDING", steps = step, boleto = boleto)
    }

    private fun getItem(): Item {
        return Item(
            catalogOfferId = CatalogOfferId(UUID.randomUUID().toString()),
            catalogOfferType = CatalogOfferType("TEST"),
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
            offerFields = OfferFields(),
            customFields = CustomFields(),
            offerItems = listOf(
                Item.OfferItem(
                    catalogOfferItemId = Item.OfferItem.CatalogOfferItemId("id"),
                    price = Price("BRL", 1000, 2)
                )
            )
        )
    }

}

class InvalidPurchaseOrderEvent() : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {

    }

}
