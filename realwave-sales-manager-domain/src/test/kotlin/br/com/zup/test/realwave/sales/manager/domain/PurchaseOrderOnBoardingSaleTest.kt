package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.updateOnBoardingSale
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderOnBoardingSaleTest {

    @Test
    fun testUpdateOnBoardingSale() {
        val purchaseOrder = buildPurchaseOrder()
        val onboardingSale = buildOnBoardingSale()
        purchaseOrder.updateOnBoardingSale(onboardingSale)
        assertEquals(expected = onboardingSale, actual = purchaseOrder.onBoardingSale)
    }

    @Test(expected = BusinessException::class)
    fun testUpdateOnBoardingSaleWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updateOnBoardingSale(buildOnBoardingSale())
    }

    private fun buildOnBoardingSale() =
        OnBoardingSale(
            offer = CatalogOfferId("catolog-offer-id"),
            customFields = null
        )

}
