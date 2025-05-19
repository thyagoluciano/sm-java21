package br.com.zup.realwave.sales.manager.consumer

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.Event
import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.service.CallbackService
import br.com.zup.realwave.sales.manager.producer.utils.eventBuilder
import br.com.zup.realwave.sales.manager.producer.utils.toStateChange
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.test.assertEquals

class PurchaseOrderKafkaConsumerTest {

    val purchaseOrderId = "27df11c8-0d27-4afe-8e09-41c44b0120b1"
    val callbackService: CallbackService = mock {}
    val repositoryManager: Repository<PurchaseOrder> = mock {}
    val purchaseOrderConsumer: PurchaseOrderConsumer = PurchaseOrderConsumer(callbackService, repositoryManager)

    @Test
    fun `Should notify via HTTP due to creation of a purchase order`(){

        val purchaseOrder = kafkaPurchaseOrderCreated()

        whenever(repositoryManager.get(AggregateId(purchaseOrderId))).doReturn(purchaseOrder)

        purchaseOrderConsumer.receive(
            topic = "topic",
            partition = 1,
            key = "key",
            message = eventBuilder(
                eventId = purchaseOrder.idAsString(),
                eventType = "PurchaseOrderCreated",
                domain = "DOMAIN",
                timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime(),
                payload = purchaseOrder.toStateChange()
            )
        )

        assertEquals(purchaseOrder.status, PurchaseOrderStatus.OPENED)
        verify(callbackService, times(1)).notify(purchaseOrder)

    }

    @Test
    fun `Should notify via HTTP due to status change of a purchase order`(){

        val purchaseOrder = kafkaPurchaseOrderCheckedOut()

        whenever(repositoryManager.get(AggregateId(purchaseOrderId))).doReturn(purchaseOrder)

        purchaseOrderConsumer.receive(
            topic = "topic",
            partition = 1,
            key = "key",
            message = eventBuilder(
                eventId = purchaseOrder.idAsString(),
                eventType = "PurchaseOrderCreated",
                domain = "DOMAIN",
                timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime(),
                payload = purchaseOrder.toStateChange()
            )
        )

        assertEquals(purchaseOrder.status, PurchaseOrderStatus.CHECKED_OUT)
        verify(callbackService, times(1)).notify(purchaseOrder)

    }

    @Test
    fun `Should notify via HTTP due to finish of a purchase order`(){

        val purchaseOrder = kafkaPurchaseOrderFinished()

        whenever(repositoryManager.get(AggregateId(purchaseOrderId))).doReturn(purchaseOrder)

        purchaseOrderConsumer.receive(
            topic = "topic",
            partition = 1,
            key = "key",
            message = eventBuilder(
                eventId = purchaseOrder.idAsString(),
                eventType = "PurchaseOrderCreated",
                domain = "DOMAIN",
                timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime(),
                payload = purchaseOrder.toStateChange()
            )
        )

        assertEquals(purchaseOrder.status, PurchaseOrderStatus.COMPLETED)
        verify(callbackService, times(1)).notify(purchaseOrder)

    }

    @Test
    fun `Should not notify via HTTP cause callback field is null`(){

        val purchaseOrder = kafkaPurchaseOrderCreatedWithoutCallback()

        whenever(repositoryManager.get(AggregateId(purchaseOrderId))).doReturn(purchaseOrder)

        purchaseOrderConsumer.receive(
            topic = "topic",
            partition = 1,
            key = "key",
            message = eventBuilder(
                eventId = purchaseOrder.idAsString(),
                eventType = "PurchaseOrderCreated",
                domain = "DOMAIN",
                timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime(),
                payload = purchaseOrder.toStateChange()
            )
        )

        assertEquals(purchaseOrder.status, PurchaseOrderStatus.OPENED)
        verify(callbackService, times(0)).notify(purchaseOrder)

    }

    private fun kafkaPurchaseOrderCreated(): PurchaseOrder {
        val purchase = PurchaseOrder()
        purchase.loadEvents(
            listOf(
                PurchaseOrderCreated(
                    aggregateId = AggregateId(purchaseOrderId),
                    purchaseOrderType = PurchaseOrderType.JOIN,
                    callback = Callback(url = "url", headers = null)
                )
            )
        )
        return purchase
    }

    private fun kafkaPurchaseOrderCreatedWithoutCallback(): PurchaseOrder {
        val purchase = PurchaseOrder()
        purchase.loadEvents(
            listOf(
                PurchaseOrderCreated(
                    aggregateId = AggregateId(purchaseOrderId),
                    purchaseOrderType = PurchaseOrderType.JOIN
                )
            )
        )
        return purchase
    }

    private fun kafkaPurchaseOrderCheckedOut(): PurchaseOrder {
        val purchase = PurchaseOrder()
        purchase.loadEvents(
            listOf(
                PurchaseOrderCreated(
                    aggregateId = AggregateId(purchaseOrderId),
                    purchaseOrderType = PurchaseOrderType.JOIN,
                    callback = Callback(url = "url", headers = null)
                ),
                PurchaseOrderCheckedOut(
                    aggregateId = AggregateId(purchaseOrderId),
                    customerOrder = CustomerOrder(
                        customerOrderId = UUID.randomUUID().toString(),
                        steps = null,
                        status = "partial"
                    ),
                    channel = Channel(value = "channel"),
                    securityCodeInformed = listOf(
                        SecurityCodeInformed("methodId", false)
                    )
                )
            )
        )
        return purchase
    }

    private fun kafkaPurchaseOrderFinished(): PurchaseOrder {
        val purchase = PurchaseOrder()
        purchase.loadEvents(
            listOf(
                PurchaseOrderCreated(
                    aggregateId = AggregateId(purchaseOrderId),
                    purchaseOrderType = PurchaseOrderType.JOIN,
                    callback = Callback(url = "url", headers = null)
                ),
                PurchaseOrderCheckedOut(
                    aggregateId = AggregateId(purchaseOrderId),
                    customerOrder = CustomerOrder(
                        customerOrderId = UUID.randomUUID().toString(),
                        steps = null,
                        status = "partial"
                    ),
                    channel = Channel(value = "channel"),
                    securityCodeInformed = listOf(
                        SecurityCodeInformed("methodId", false)
                    )
                ),
                PurchaseOrderStatusUpdated(
                    aggregateId = AggregateId(purchaseOrderId),
                    status = PurchaseOrderStatus.COMPLETED
                ),
                PurchaseOrderInstallationAttributesUpdated(
                    aggregateId = AggregateId(purchaseOrderId),
                    installationAttribute = InstallationAttribute(
                        productTypeId = ProductTypeId("product_type_id"),
                        attributes = mapOf(
                            "iccid" to "12334532123123",
                            "number" to "33234321"
                        )
                    )
                )
            )
        )
        return purchase
    }

}

fun PurchaseOrder.loadEvents(events: List<Event>) {
    for (event: Event in events) {
        applyChange(event)
    }
}
