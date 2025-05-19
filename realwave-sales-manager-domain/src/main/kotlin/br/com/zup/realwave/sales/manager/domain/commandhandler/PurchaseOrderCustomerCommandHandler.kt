package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateCustomerCommand
import br.com.zup.realwave.sales.manager.domain.updateCustomer
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCustomerCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateCustomerCommand): PurchaseOrderId {
        log.debug(
            "Received command to update customer [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.customer, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateCustomer(command.customer)
        }
        log.debug("Updated customer [{}] to purchaseOrderId: {}", command.customer, command.id)
        return command.id
    }

}
