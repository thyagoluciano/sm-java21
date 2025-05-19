package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.command.CheckoutCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateCustomerOrderCommand
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckoutResolver
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.domain.updateCustomerOrder
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCustomerOrderCommandHandler(
    val purchaseOrderValidator: PurchaseOrderValidator,
    val checkoutFactoryService: PurchaseOrderCheckoutResolver,
    val purchaseOrderProducer: PurchaseOrderProducer
) : BaseCommandHandler() {

    fun handle(command: CheckoutCommand): CustomerOrder {
        log.debug("Received command to checkout Purchase Order in event store with id: {} to tenant [{}]", command.id)
        lateinit var customerOrder: CustomerOrder
        val purchaseOrder = withPurchaseOrder(command.id) { purchaseOrder ->
            customerOrder = command.execute(
                purchaseOrder, command.channel,
                checkoutFactoryService,
                purchaseOrderValidator,
                command.securityCodes
            )
        }
        purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder)
        log.debug("Checkout generated purchase order. Id: {}", command.id)
        return customerOrder
    }

    fun handle(command: UpdateCustomerOrderCommand) {
        log.debug(
            "Update customer order status [{}] in event store to purchaseOrderId: [{}]",
            command.customerOrder, command.id, command.reason
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateCustomerOrder(command.customerOrder, purchaseOrderProducer, command.reason)
        }
        log.debug("Updated customer order status [{}] to purchaseOrderId: [{}]", command.customerOrder, command.id)
    }

}
