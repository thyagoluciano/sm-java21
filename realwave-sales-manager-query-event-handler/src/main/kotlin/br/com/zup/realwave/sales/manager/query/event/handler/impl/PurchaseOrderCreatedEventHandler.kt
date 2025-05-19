package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCreatedEventHandler : BaseEventHandler<PurchaseOrderCreated>() {

    override fun handle(event: PurchaseOrderCreated, metaData: MetaData, version: AggregateVersion) {
        log.debug("Received event to save purchase order in database with id: [{}]", event.aggregateId)
        val context = RealwaveContextHolder.getContext()
        val channel: Channel = if (null != context?.channel) Channel(context.channel) else Channel("UNKNOWN")
        purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = PurchaseOrderId(event.aggregateId.value),
            channelCreate = channel,
            callback = event.callback,
            customer = event.customer,
            version = version.value,
            type = event.purchaseOrderType
        )
        log.debug("Saved purchase order in database with id: [{}]", event.aggregateId)
    }

}
