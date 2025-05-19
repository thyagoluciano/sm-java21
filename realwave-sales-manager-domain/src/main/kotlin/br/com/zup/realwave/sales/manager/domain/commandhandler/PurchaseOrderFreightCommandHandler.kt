package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateFreightCommand
import br.com.zup.realwave.sales.manager.domain.updateFreight
import org.springframework.stereotype.Service

@Service
class PurchaseOrderFreightCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateFreightCommand): PurchaseOrderId {
        log.debug(
            "Received command to update freight [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.freight,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateFreight(command.freight)
        }
        log.debug("Updated freight [{}] to purchaseOrderId: {}", command.freight, command.id)
        return command.id
    }

}
