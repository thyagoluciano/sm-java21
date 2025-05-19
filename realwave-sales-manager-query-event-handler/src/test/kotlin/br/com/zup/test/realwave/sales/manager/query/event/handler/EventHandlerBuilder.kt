package br.com.zup.test.realwave.sales.manager.query.event.handler

import br.com.zup.realwave.sales.manager.domain.repository.CustomerOrderRepository
import br.com.zup.realwave.sales.manager.domain.repository.FreightRepository
import br.com.zup.realwave.sales.manager.domain.repository.PaymentRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.query.event.handler.impl.BaseEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.EventHandlerDiscoveryService
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderCheckedOutEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderCouponUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderCreatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderCustomerOrderUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderCustomerUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderDeletedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderFreightUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderInstallationAttributesDeletedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderInstallationAttributesUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderItemAddedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderItemRemovedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderItemUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderMgmDeletedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderMgmUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderOnBoardingSaleUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderPaymentUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderProtocolUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderReasonStatusUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderSalesForceRemovedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderSalesForceUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderSegmentationUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderStatusUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderSubscriptionUpdatedEventHandler
import br.com.zup.realwave.sales.manager.query.event.handler.impl.PurchaseOrderTypeUpdatedEventHandler

fun buildEventHandlerDiscoveryService(
    purchaseOrderRepository: PurchaseOrderRepository,
    customerOrderRepository: CustomerOrderRepository,
    paymentRepository: PaymentRepository,
    freightRepository: FreightRepository,
    handlers: MutableList<BaseEventHandler<*>> = mutableListOf()
) = EventHandlerDiscoveryService(
    createdEventHandler = PurchaseOrderCreatedEventHandler().also { handlers.add(it) },
    segmentationUpdatedEventHandler = PurchaseOrderSegmentationUpdatedEventHandler().also { handlers.add(it) },
    mgmUpdatedEventHandler = PurchaseOrderMgmUpdatedEventHandler().also { handlers.add(it) },
    mgmDeletedEventHandler = PurchaseOrderMgmDeletedEventHandler().also { handlers.add(it) },
    customerUpdatedEventHandler = PurchaseOrderCustomerUpdatedEventHandler().also { handlers.add(it) },
    onBoardingSaleUpdatedEventHandler = PurchaseOrderOnBoardingSaleUpdatedEventHandler().also { handlers.add(it) },
    couponUpdatedEventHandler = PurchaseOrderCouponUpdatedEventHandler().also { handlers.add(it) },
    paymentUpdatedEventHandler = PurchaseOrderPaymentUpdatedEventHandler().also { handlers.add(it) },
    deletedEventHandler = PurchaseOrderDeletedEventHandler().also { handlers.add(it) },
    installationAttributesUpdatedEventHandler = PurchaseOrderInstallationAttributesUpdatedEventHandler().also {
        handlers.add(it)
    },
    installationAttributesDeletedEventHandler = PurchaseOrderInstallationAttributesDeletedEventHandler().also {
        handlers.add(it)
    },
    itemAddedEventHandler = PurchaseOrderItemAddedEventHandler().also { handlers.add(it) },
    itemRemovedEventHandler = PurchaseOrderItemRemovedEventHandler().also { handlers.add(it) },
    itemUpdatedEventHandler = PurchaseOrderItemUpdatedEventHandler().also { handlers.add(it) },
    checkedOutEventHandler = PurchaseOrderCheckedOutEventHandler().also { handlers.add(it) },
    customerOrderUpdatedEventHandler = PurchaseOrderCustomerOrderUpdatedEventHandler().also { handlers.add(it) },
    protocolUpdatedEventHandler = PurchaseOrderProtocolUpdatedEventHandler().also { handlers.add(it) },
    subscriptionUpdatedEventHandler = PurchaseOrderSubscriptionUpdatedEventHandler().also { handlers.add(it) },
    typeUpdatedEventHandler = PurchaseOrderTypeUpdatedEventHandler().also { handlers.add(it) },
    reasonStatusUpdatedEventHandler = PurchaseOrderReasonStatusUpdatedEventHandler().also { handlers.add(it) },
    statusUpdatedEventHandler = PurchaseOrderStatusUpdatedEventHandler().also { handlers.add(it) },
    salesForceRemovedEventHandler = PurchaseOrderSalesForceRemovedEventHandler().also { handlers.add(it) },
    salesForceUpdatedEventHandler = PurchaseOrderSalesForceUpdatedEventHandler().also { handlers.add(it) },
    purchaseOrderFreightUpdatedEventHandler = PurchaseOrderFreightUpdatedEventHandler().also { handlers.add(it) }
).also {
    handlers.forEach {
        it.purchaseOrderRepository = purchaseOrderRepository
        it.customerOrderRepository = customerOrderRepository
        it.paymentRepository = paymentRepository
        it.freightRepository = freightRepository
    }
}
