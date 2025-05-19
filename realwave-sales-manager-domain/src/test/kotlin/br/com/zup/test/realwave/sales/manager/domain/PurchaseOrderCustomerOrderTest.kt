package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.updateCustomerOrder
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID
import kotlin.test.assertEquals

class PurchaseOrderCustomerOrderTest {

    val producer = Mockito.mock(PurchaseOrderProducer::class.java)

    @Test
    fun updateCustomerOrderCompletedSuccess() {
        val purchaseOrder = buildPurchaseOrder()

        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "completed"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, null)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.COMPLETED, purchaseOrder.status)
    }

    @Test
    fun updateCustomerOrderCompletedFailedPartial() {
        val purchaseOrder = buildPurchaseOrder()
        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "partial"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, null)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.OPENED, purchaseOrder.status)
    }

    @Test
    fun updateCustomerOrderCompletedFailedRejected() {
        val purchaseOrder = buildPurchaseOrder()
        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "rejected"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, null)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.OPENED, purchaseOrder.status)
    }

    @Test
    fun updateCustomerOrderCompletedFailedFailed() {
        val purchaseOrder = buildPurchaseOrder()
        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "failed"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, null)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.OPENED, purchaseOrder.status)
    }

    @Test
    fun updateCustomerOrderCompletedFailedCancel() {
        val purchaseOrder = buildPurchaseOrder()
        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "canceled"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, null)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.CANCELED, purchaseOrder.status)
    }

    @Test
    fun updateCustomerOrderCompletedReasonSuccessCancel() {
        val purchaseOrder = buildPurchaseOrder()
        val reason = Reason(code = "CANCELED", description = null)

        val customerOrder = CustomerOrder(
            customerOrderId = UUID.randomUUID().toString(),
            steps = null,
            status = "completed"
        )
        purchaseOrder.updateCustomerOrder(customerOrder, producer, reason)

        Mockito.verify(producer).notifyPurchaseOrderStateUpdated(purchaseOrder)

        assertEquals(PurchaseOrderStatus.COMPLETED, purchaseOrder.status)
        assertEquals(expected = reason.code, actual = purchaseOrder.reason?.code)
    }

}
