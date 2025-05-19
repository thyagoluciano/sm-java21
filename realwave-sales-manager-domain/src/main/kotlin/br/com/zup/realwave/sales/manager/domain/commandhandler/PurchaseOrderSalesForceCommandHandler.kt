package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.RemoveSalesForceCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateSalesForceCommand
import br.com.zup.realwave.sales.manager.domain.removeSalesForce
import br.com.zup.realwave.sales.manager.domain.updateSalesForce
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSalesForceCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateSalesForceCommand): PurchaseOrderId {
        log.debug(
            "Received command to update salesForce [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.salesForce,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateSalesForce(command.salesForce)
        }
        log.debug("Updated salesForce [{}] to purchaseOrderId: {}", command.salesForce, command.id)
        return command.id
    }

    fun handle(command: RemoveSalesForceCommand): PurchaseOrderId {
        log.debug(
            "Received command to remove salesForce [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.removeSalesForce()
        }
        log.debug("Removed salesForce [{}] to purchaseOrderId: {}", command.id)
        return command.id
    }

}
