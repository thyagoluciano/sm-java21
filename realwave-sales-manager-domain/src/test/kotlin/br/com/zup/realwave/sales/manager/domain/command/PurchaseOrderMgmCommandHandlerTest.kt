package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderMgmCommandHandler
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PurchaseOrderMgmCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val mgmService = mockk<MemberGetMemberService>()

    val mgmCommandHandler = PurchaseOrderMgmCommandHandler(mgmService).apply {
        this.repositoryManager = repository
    }

    @Test
    fun updateMgm() {
        val purchaseOrder = buildPurchaseOrder()
        val mgm = Mgm("mgm-code");
        val command = UpdateMgmCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            mgm = mgm
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder
        every { mgmService.validate(any()) } returns Unit

        mgmCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }
        verify { mgmService.validate(mgm.code) }

        assertEquals(expected = mgm, actual = purchaseOrder.mgm)
    }

    @Test
    fun deleteMgm() {
        val purchaseOrder = buildPurchaseOrder()
            .apply {
                mgm = Mgm("mgm-code");
            }

        val command = DeleteMgmCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString())
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        mgmCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertNull(purchaseOrder.mgm)
    }

}
