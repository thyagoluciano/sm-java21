package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated
import br.com.zup.realwave.sales.manager.domain.removeSalesForce
import br.com.zup.realwave.sales.manager.domain.updateSalesForce
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PurchaseOrderSalesForceTest {

    @Test
    fun updateSalesForce() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val salesForce = SalesForce(id = "id", name = "name")

        purchaseOrder.updateSalesForce(salesForce)

        assertNotNull(purchaseOrder.salesForce)
        assertEquals(expected = salesForce, actual = purchaseOrder.salesForce)
        assertTrue(purchaseOrder.event is PurchaseOrderSalesForceUpdated)
    }

    @Test(expected = BusinessException::class)
    fun updateSalesForceWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        val salesForce = SalesForce(id = "id", name = "name")
        purchaseOrder.updateSalesForce(salesForce)
    }

    @Test
    fun removeSalesForce() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        purchaseOrder.salesForce = SalesForce(id = "id", name = "name")

        purchaseOrder.removeSalesForce()

        assertNull(purchaseOrder.salesForce)
        assertTrue(purchaseOrder.event is PurchaseOrderSalesForceRemoved)
    }

    @Test(expected = BusinessException::class)
    fun removeSalesForceWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.CHECKED_OUT)
        purchaseOrder.salesForce = SalesForce(id = "id", name = "name")
        purchaseOrder.removeSalesForce()
    }

}
