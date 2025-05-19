package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.canCheckout
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckoutResolver
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.domain.verifyPurchaseOrderIsOpen
import org.apache.logging.log4j.LogManager

data class CheckoutCommand(
    val id: PurchaseOrderId,
    val channel: Channel,
    val securityCodes: List<SecurityCode>
) {

    private val log = LogManager.getLogger(this.javaClass)

    fun execute(
        purchaseOrder: PurchaseOrder,
        channel: Channel,
        checkoutFactory: PurchaseOrderCheckoutResolver,
        purchaseOrderValidator: PurchaseOrderValidator,
        securityCodes: List<SecurityCode> = emptyList()
    ): CustomerOrder {
        purchaseOrder.verifyPurchaseOrderIsOpen()
        purchaseOrderValidator.validate(purchaseOrder)
        purchaseOrder.canCheckout()

        val purchaseOrderCheckout = checkoutFactory.resolve(purchaseOrder = purchaseOrder)

        purchaseOrder.customerOrder = purchaseOrderCheckout.checkout(
            purchaseOrder = purchaseOrder,
            channel = channel,
            securityCode = securityCodes
        )

        log.info("Begin apply change PurchaseOrderCheckedOut on eventstore")
        purchaseOrder.applyChange(PurchaseOrderCheckedOut(
            aggregateId = id,
            customerOrder = purchaseOrder.customerOrder,
            channel = channel,
            securityCodeInformed = securityCodes.map {
                SecurityCodeInformed(
                    methodId = it.methodId,
                    securityCodeInformed = !it.securityCode.isBlank()
                )
            }
        ))
        log.info("End apply change PurchaseOrderCheckedOut on eventstore")
        return purchaseOrder.customerOrder!!
    }

}
