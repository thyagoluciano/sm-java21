package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateProtocolCommand
import br.com.zup.realwave.sales.manager.domain.updateProtocol
import org.springframework.stereotype.Service

@Service
class PurchaseOrderProtocolCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateProtocolCommand): PurchaseOrderId {
        log.debug(
            "Received command to update protocol [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.protocol, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateProtocol(command.protocol)
        }
        log.debug("Updated protocol [{}] to purchaseOrderId: {}", command.protocol, command.id)
        return command.id
    }

}
