package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCouponUpdatedEventHandler : BaseEventHandler<PurchaseOrderCouponUpdated>() {

    override fun handle(event: PurchaseOrderCouponUpdated, metaData: MetaData, version: AggregateVersion) {
        log.debug(
            "Received event to update couponCode [{}] in database for purchaseOrderId: [{}]",
            event.coupon,
            event.aggregateId
        )
        purchaseOrderRepository.updateCoupon(PurchaseOrderId(event.aggregateId.value), event.coupon, version.value)
        log.debug(
            "Updated on couponCode [{}] in database with for purchaseOrderId: [{}]",
            event.coupon,
            event.aggregateId
        )
    }

}
