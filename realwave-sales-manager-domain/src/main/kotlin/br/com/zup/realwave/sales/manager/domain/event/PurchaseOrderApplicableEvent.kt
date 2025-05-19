package br.com.zup.realwave.sales.manager.domain.event

import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder

abstract class PurchaseOrderApplicableEvent : Event() {

    abstract fun apply(purchaseOrder: PurchaseOrder)

}

