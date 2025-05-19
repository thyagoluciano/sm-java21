package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateSubscriptionCommand
import br.com.zup.realwave.sales.manager.domain.updateSubscription
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSubscriptionCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateSubscriptionCommand): PurchaseOrderId {
        log.debug(
            "Received command to update subscriptionId [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.subscriptionId,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateSubscription(command.subscriptionId)
        }
        log.debug("Updated subscriptionId [{}] to purchaseOrderId: {}", command.subscriptionId, command.id)
        return command.id
    }

}
