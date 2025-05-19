package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated
import br.com.zup.realwave.sales.manager.domain.updateProtocol
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class PurchaseOrderProtocolTest {

    @Test
    fun updateProtocol() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val protocol = Protocol("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        purchaseOrder.updateProtocol(protocol)

        Assert.assertNotNull(purchaseOrder.protocol)
        assertEquals(protocol.value, purchaseOrder.protocol)
        Assert.assertTrue(purchaseOrder.event is PurchaseOrderProtocolUpdated)
    }

    @Test(expected = BusinessException::class)
    fun testUpdateProtocolWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        val protocol = Protocol("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        purchaseOrder.updateProtocol(protocol)
    }

}
