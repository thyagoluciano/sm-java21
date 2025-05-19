package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCheckedOut
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerOrderUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderOnBoardingSaleUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderProtocolUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderReasonStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceRemoved
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSalesForceUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderTypeUpdated
import org.springframework.stereotype.Service

@Service
class EventHandlerDiscoveryService(
    val createdEventHandler: PurchaseOrderCreatedEventHandler,
    val segmentationUpdatedEventHandler: PurchaseOrderSegmentationUpdatedEventHandler,
    val mgmUpdatedEventHandler: PurchaseOrderMgmUpdatedEventHandler,
    val mgmDeletedEventHandler: PurchaseOrderMgmDeletedEventHandler,
    val customerUpdatedEventHandler: PurchaseOrderCustomerUpdatedEventHandler,
    val onBoardingSaleUpdatedEventHandler: PurchaseOrderOnBoardingSaleUpdatedEventHandler,
    val couponUpdatedEventHandler: PurchaseOrderCouponUpdatedEventHandler,
    val paymentUpdatedEventHandler: PurchaseOrderPaymentUpdatedEventHandler,
    val deletedEventHandler: PurchaseOrderDeletedEventHandler,
    val installationAttributesUpdatedEventHandler: PurchaseOrderInstallationAttributesUpdatedEventHandler,
    val installationAttributesDeletedEventHandler: PurchaseOrderInstallationAttributesDeletedEventHandler,
    val itemAddedEventHandler: PurchaseOrderItemAddedEventHandler,
    val itemRemovedEventHandler: PurchaseOrderItemRemovedEventHandler,
    val itemUpdatedEventHandler: PurchaseOrderItemUpdatedEventHandler,
    val checkedOutEventHandler: PurchaseOrderCheckedOutEventHandler,
    val customerOrderUpdatedEventHandler: PurchaseOrderCustomerOrderUpdatedEventHandler,
    val protocolUpdatedEventHandler: PurchaseOrderProtocolUpdatedEventHandler,
    val subscriptionUpdatedEventHandler: PurchaseOrderSubscriptionUpdatedEventHandler,
    val typeUpdatedEventHandler: PurchaseOrderTypeUpdatedEventHandler,
    val reasonStatusUpdatedEventHandler: PurchaseOrderReasonStatusUpdatedEventHandler,
    val statusUpdatedEventHandler: PurchaseOrderStatusUpdatedEventHandler,
    val salesForceRemovedEventHandler: PurchaseOrderSalesForceRemovedEventHandler,
    val salesForceUpdatedEventHandler: PurchaseOrderSalesForceUpdatedEventHandler,
    val purchaseOrderFreightUpdatedEventHandler: PurchaseOrderFreightUpdatedEventHandler
) {

    fun <T : Event> getEventHandler(event: T): BaseEventHandler<T> =
        when (event) {
            is PurchaseOrderCreated -> createdEventHandler
            is PurchaseOrderSegmentationUpdated -> segmentationUpdatedEventHandler
            is PurchaseOrderMgmUpdated -> mgmUpdatedEventHandler
            is PurchaseOrderMgmDeleted -> mgmDeletedEventHandler
            is PurchaseOrderCustomerUpdated -> customerUpdatedEventHandler
            is PurchaseOrderOnBoardingSaleUpdated -> onBoardingSaleUpdatedEventHandler
            is PurchaseOrderCouponUpdated -> couponUpdatedEventHandler
            is PurchaseOrderPaymentUpdated -> paymentUpdatedEventHandler
            is PurchaseOrderDeleted -> deletedEventHandler
            is PurchaseOrderInstallationAttributesUpdated -> installationAttributesUpdatedEventHandler
            is PurchaseOrderInstallationAttributesDeleted -> installationAttributesDeletedEventHandler
            is PurchaseOrderItemAdded -> itemAddedEventHandler
            is PurchaseOrderItemRemoved -> itemRemovedEventHandler
            is PurchaseOrderItemUpdated -> itemUpdatedEventHandler
            is PurchaseOrderCheckedOut -> checkedOutEventHandler
            is PurchaseOrderCustomerOrderUpdated -> customerOrderUpdatedEventHandler
            is PurchaseOrderProtocolUpdated -> protocolUpdatedEventHandler
            is PurchaseOrderSubscriptionUpdated -> subscriptionUpdatedEventHandler
            is PurchaseOrderTypeUpdated -> typeUpdatedEventHandler
            is PurchaseOrderReasonStatusUpdated -> reasonStatusUpdatedEventHandler
            is PurchaseOrderStatusUpdated -> statusUpdatedEventHandler
            is PurchaseOrderSalesForceRemoved -> salesForceRemovedEventHandler
            is PurchaseOrderSalesForceUpdated -> salesForceUpdatedEventHandler
            is PurchaseOrderFreightUpdated -> purchaseOrderFreightUpdatedEventHandler
            else -> throw IllegalArgumentException("Invalid event type ${event.javaClass.canonicalName}")
        } as BaseEventHandler<T>

}
