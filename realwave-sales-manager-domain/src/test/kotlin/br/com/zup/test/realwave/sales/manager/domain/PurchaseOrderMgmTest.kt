package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.deleteMgm
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.domain.updateMgm
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PurchaseOrderMgmTest {

    val mgmService = Mockito.mock(MemberGetMemberService::class.java)

    @Test
    fun updateMgm() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val mgm = Mgm("MGM-CODE");

        purchaseOrder.updateMgm(mgm, mgmService)

        Mockito.verify(mgmService).validate(mgm.code)
        assertEquals(expected = mgm, actual = purchaseOrder.mgm)
    }

    @Test(expected = BusinessException::class)
    fun updateMgmOfInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updateMgm(Mgm("MGM-CODE"), mgmService)
    }

    @Test
    fun removeMgm() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)

        purchaseOrder.mgm = Mgm("MGM-CODE")
        purchaseOrder.deleteMgm()

        assertNull(purchaseOrder.mgm)
    }

}
