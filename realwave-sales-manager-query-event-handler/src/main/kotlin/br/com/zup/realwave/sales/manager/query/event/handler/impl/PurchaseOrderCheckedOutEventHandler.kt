package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCheckedOutEventHandler : BaseEventHandler<PurchaseOrderCheckedOut>() {

    override fun handle(event: PurchaseOrderCheckedOut, metaData: MetaData, version: AggregateVersion) {
        log.debug("Received event to apply checkout to purchase order in database with id: [{}]", event.aggregateId)

        if (event.customerOrder != null) {
            customerOrderRepository.saveCustomerOrder(event.aggregateId, event.customerOrder!!)
            paymentRepository.takeIf {
                event.customerOrder?.boleto != null
            }?.updateMethodId(
                purchaseOrderId = event.aggregateId,
                methodId = event.customerOrder!!.boleto!!.methodId
            )
        }

        purchaseOrderRepository.checkOutPurchaseOrder(
            event.aggregateId,
            event.channel,
            version.value,
            event.securityCodeInformed
        )
        log.debug("Applied checkout to purchase order in database with id: [{}]", event.aggregateId)
    }

}
