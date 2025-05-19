package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdatePaymentCommand
import br.com.zup.realwave.sales.manager.domain.updatePayment
import org.springframework.stereotype.Service

@Service
class PurchaseOrderPaymentCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdatePaymentCommand): PurchaseOrderId {
        log.debug(
            "Received command to update payment [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.payment, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updatePayment(command.payment)
        }
        log.debug("Updated payment [{}] to purchaseOrderId: {}", command.payment, command.id)
        return command.id
    }

}
