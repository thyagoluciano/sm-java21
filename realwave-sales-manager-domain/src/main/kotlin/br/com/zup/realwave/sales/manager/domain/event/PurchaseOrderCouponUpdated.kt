package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

class PurchaseOrderCouponUpdated(
    val aggregateId: AggregateId,
    val coupon: CouponCode
) : PurchaseOrderApplicableEvent() {

    override fun apply(purchaseOrder: PurchaseOrder) {
        purchaseOrder.coupon = coupon
    }

}
