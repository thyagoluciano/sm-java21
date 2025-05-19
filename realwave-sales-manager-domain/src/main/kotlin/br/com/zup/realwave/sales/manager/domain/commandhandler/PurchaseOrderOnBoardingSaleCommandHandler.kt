package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateOnBoardingSaleCommand
import br.com.zup.realwave.sales.manager.domain.updateOnBoardingSale
import org.springframework.stereotype.Service

@Service
class PurchaseOrderOnBoardingSaleCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateOnBoardingSaleCommand): PurchaseOrderId {
        log.debug(
            "Received command to update on boarding sale [{}] in event store to purchaseOrderId: {} to tenant " +
                    "[{}]", command.onBoardingSale, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateOnBoardingSale(command.onBoardingSale)
        }
        log.debug("Updated on boarding sale [{}] to purchaseOrderId: {}", command.onBoardingSale, command.id)
        return command.id
    }

}
