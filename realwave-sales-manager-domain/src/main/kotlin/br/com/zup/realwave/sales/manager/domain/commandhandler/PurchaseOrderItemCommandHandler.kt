package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.addItem
import br.com.zup.realwave.sales.manager.domain.command.AddItemCommand
import br.com.zup.realwave.sales.manager.domain.command.RemoveItemCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateItemCommand
import br.com.zup.realwave.sales.manager.domain.removeItem
import br.com.zup.realwave.sales.manager.domain.updateItem
import org.springframework.stereotype.Service

@Service
class PurchaseOrderItemCommandHandler : BaseCommandHandler() {

    fun handle(command: AddItemCommand): Item.Id {
        log.debug(
            "Received command to adding item [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.item, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.addItem(command.item)
        }
        log.debug("Added item [{}] to purchaseOrderId: {}", command.item, command.id)
        return command.item.id
    }

    fun handle(command: UpdateItemCommand): Item.Id {
        log.debug(
            "Received command to update item [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.item, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateItem(command.item)
        }
        log.debug("Updated item [{}] to purchaseOrderId: {}", command.item, command.id)
        return command.item.id
    }

    fun handle(command: RemoveItemCommand): PurchaseOrderId {
        log.debug(
            "Received command to remove item [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.itemId, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.removeItem(command.itemId)
        }
        log.debug("Removed item [{}] to purchaseOrderId: {}", command.itemId, command.id)
        return command.id
    }

}
