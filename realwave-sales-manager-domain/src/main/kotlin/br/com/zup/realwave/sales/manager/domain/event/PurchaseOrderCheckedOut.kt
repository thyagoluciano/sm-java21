package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed

class PurchaseOrderCheckedOut(
    val aggregateId: AggregateId,
    val customerOrder: CustomerOrder? = null,
    val channel: Channel,
    val securityCodeInformed: List<SecurityCodeInformed>
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.status = PurchaseOrderStatus.CHECKED_OUT
        purchaseOrder.customerOrder = customerOrder
        purchaseOrder.channelCheckout = channel
        purchaseOrder.securityCodeInformed = securityCodeInformed
    }

}
