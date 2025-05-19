package br.com.zup.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerOrderUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderReasonStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer

fun PurchaseOrder.updateCustomerOrder(customerOrder: CustomerOrder, producer: PurchaseOrderProducer, reason: Reason?) {
    applyChange(PurchaseOrderCustomerOrderUpdated(aggregateId = id, customerOrder = customerOrder))
    if (reason != null) updatePurchaseOrderReasonStatus(id, reason)
    if (isCustomerOrderCompletedSuccess(customerOrder)) updatePurchaseStatus(
        producer,
        PurchaseOrderStatus.COMPLETED
    )
    if (isCustomerOrderCompletedFailed(customerOrder)) updatePurchaseStatus(
        producer,
        PurchaseOrderStatus.OPENED
    )
    if (isCustomerOrderCanceled(customerOrder)) updatePurchaseStatus(producer, PurchaseOrderStatus.CANCELED)
}

internal fun PurchaseOrder.updatePurchaseOrderReasonStatus(purchaseOrderId: AggregateId, reason: Reason?) {
    applyChange(PurchaseOrderReasonStatusUpdated(purchaseOrderId, reason))
}

internal fun PurchaseOrder.updatePurchaseStatus(producer: PurchaseOrderProducer, status: PurchaseOrderStatus) {
    applyChange(PurchaseOrderStatusUpdated(id, status))
    producer.notifyPurchaseOrderStateUpdated(this)
}

internal fun isCustomerOrderCompletedSuccess(customerOrder: CustomerOrder): Boolean =
    customerOrder.status!!.toUpperCase() == "COMPLETED"

internal fun isCustomerOrderCanceled(customerOrder: CustomerOrder): Boolean =
    customerOrder.status!!.toUpperCase() == "CANCELED"

internal fun isCustomerOrderCompletedFailed(customerOrder: CustomerOrder): Boolean =
    customerOrder.status!!.toUpperCase() == "PARTIAL" ||
            customerOrder.status.toUpperCase() == "REJECTED" ||
            customerOrder.status.toUpperCase() == "FAILED"
