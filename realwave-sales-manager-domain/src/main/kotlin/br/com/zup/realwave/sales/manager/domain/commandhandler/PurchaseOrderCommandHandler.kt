package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.CreatePurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.DeletePurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.FindPurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdatePurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.command.ValidatePurchaseOrder
import br.com.zup.realwave.sales.manager.domain.deletePurchaseOrder
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.domain.updatePurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.validatePurchaseOrder
import org.springframework.stereotype.Service

/**
 * Created by cleber on 5/29/17.
 */
@Service
class PurchaseOrderCommandHandler(
    val purchaseOrderValidator: PurchaseOrderValidator,
    val purchaseOrderProducer: PurchaseOrderProducer
) : BaseCommandHandler() {

    fun handle(command: CreatePurchaseOrderCommand): PurchaseOrder {
        log.debug(
            "Received command to create Purchase Order in event store with id: {} to tenant [{}]",
            command.id
        )
        val purchaseOrder = PurchaseOrder(
            aggregateId = command.id,
            purchaseOrderType = command.purchaseOrderType,
            customer = command.customer, callback = command.callback
        )
        repositoryManager.save(purchaseOrder)
        purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder)
        log.debug("Created purchase order. Id: {}", command.id)
        return purchaseOrder
    }

    fun handle(command: UpdatePurchaseOrderType): PurchaseOrderId {
        log.debug(
            "Received command to update type [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.type,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updatePurchaseOrderType(command.type)
        }
        log.debug("Updated type [{}] to purchaseOrderId: {}", command.type, command.id)
        return command.id
    }

    fun handle(command: DeletePurchaseOrderCommand): PurchaseOrderId {
        log.debug(
            "Received command to delete Purchase Order in event store with id: {} to tenant [{}]",
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.deletePurchaseOrder()
        }
        log.debug("Deleted purchase order. Id: {}", command.id)
        return command.id
    }

    fun handle(command: ValidatePurchaseOrder) {
        log.debug(
            "Received command to validity Purchase Order in event store with id: {} to tenant [{}]",
            command.id
        )
        val purchaseOrder = getPurchaseOrder(command.id)
        purchaseOrder.validatePurchaseOrder(purchaseOrderValidator)
        log.debug("Validated purchase order. Id: {}")
    }

    fun handle(command: FindPurchaseOrderCommand): PurchaseOrder {
        log.debug(
                "Received command to find Purchase Order in event store with id: {} to tenant [{}]",
                command.id
        )
        return getPurchaseOrder(command.id)
    }

}
