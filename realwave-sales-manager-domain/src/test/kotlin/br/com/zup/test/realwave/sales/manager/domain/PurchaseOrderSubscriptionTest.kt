package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import br.com.zup.realwave.sales.manager.domain.updateSubscription
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class PurchaseOrderSubscriptionTest {

    @Test
    fun updateSubscription() {
        var purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.CHANGE)
        var subscription = Subscription("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        purchaseOrder.updateSubscription(subscription)

        Assert.assertNotNull(purchaseOrder.subscriptionId)
        assertEquals(subscription.id, purchaseOrder.subscriptionId)
        Assert.assertTrue(purchaseOrder.event is PurchaseOrderSubscriptionUpdated)
    }

    @Test
    fun updatePurchaseOrderSubscription() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.CHANGE)
        purchaseOrder.updateSubscription(Subscription("subscription-id"))

        assertEquals("subscription-id", purchaseOrder.subscriptionId)
        Assert.assertTrue(purchaseOrder.event is PurchaseOrderSubscriptionUpdated)
    }

    @Test(expected = BusinessException::class)
    fun updateSubscriptionWithInvalidPurchaseOrderStatus() {
        var purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.CANCELED)
        var subscription = Subscription("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        purchaseOrder.updateSubscription(subscription)
    }

}
