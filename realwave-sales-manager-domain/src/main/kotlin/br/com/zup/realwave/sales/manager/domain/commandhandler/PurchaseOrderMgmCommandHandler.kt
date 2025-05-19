package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.DeleteMgmCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateMgmCommand
import br.com.zup.realwave.sales.manager.domain.deleteMgm
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.domain.updateMgm
import org.springframework.stereotype.Service

@Service
class PurchaseOrderMgmCommandHandler(
    val memberGetMemberService: MemberGetMemberService
) : BaseCommandHandler() {

    fun handle(command: UpdateMgmCommand): PurchaseOrderId {
        log.debug(
            "Received command to update mgm [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.mgm, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateMgm(command.mgm, memberGetMemberService)
        }
        log.debug("Updated mgm [{}] to purchaseOrderId: {}", command.mgm, command.id)
        return command.id
    }

    fun handle(command: DeleteMgmCommand): PurchaseOrderId {
        log.debug(
            "Received command to remove mgm [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.deleteMgm()
        }
        log.debug("Delete mgm code to purchaseOrderId: {}", command.id)
        return command.id
    }

}
