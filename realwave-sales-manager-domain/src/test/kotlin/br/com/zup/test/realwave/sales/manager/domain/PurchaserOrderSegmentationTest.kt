package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.updateSegmentation
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class PurchaserOrderSegmentationTest {

    @Test
    fun addSegmentationSuccess() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val segmentation = segmentation()
        purchaseOrder.updateSegmentation(segmentation)

        Assert.assertNotNull(purchaseOrder.segmentation)
        assertEquals(segmentation.query, purchaseOrder.segmentation!!.query)
    }

    @Test(expected = BusinessException::class)
    fun addSegmentationError() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        purchaseOrder.status = PurchaseOrderStatus.DELETED
        purchaseOrder.updateSegmentation(segmentation())
    }

}
