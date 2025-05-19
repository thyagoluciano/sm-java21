package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.command.CreatePurchaseOrderCouponCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateCouponCommand
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.updateCoupon
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PurchaseOrderCouponCommandHandler(
    val couponService: CouponService,
    val purchaseOrderProducer: PurchaseOrderProducer
) : BaseCommandHandler() {

    @Value("\${event.sourcing.force.optimistic.lock.update.coupon}")
    private val optimisticLock: Repository.OptimisticLock = Repository.OptimisticLock.DISABLED

    fun handle(command: CreatePurchaseOrderCouponCommand): PurchaseOrder {
        log.debug("Received command to CreatePurchaseOrderCouponCommand in event store with id: {}", command.id)

        val purchaseOrder = PurchaseOrder(
                aggregateId = command.id,
                purchaseOrderType = PurchaseOrderType.COUPON,
                callback =  command.callback)

        command.execute(purchaseOrder, couponService)

        repositoryManager.save(purchaseOrder)

        purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder)

        log.debug("Created purchase order. Id: {}", command.id)
        return purchaseOrder
    }

    fun handle(command: UpdateCouponCommand): PurchaseOrderId {
        log.debug(
            "Received command to update couponCode [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.coupon, command.id
        )
        log.info("Begin get purchase order in eventstore")
        val purchaseOrder = getPurchaseOrder(command.id)
        log.info("End get purchase order in eventstore")
        purchaseOrder.updateCoupon(command.coupon, couponService)
        log.info("Begin save purchase order events in eventstore, updateCoupon lock: $optimisticLock")
        repositoryManager.save(purchaseOrder, optimisticLock)
        log.debug("Updated couponCode [{}] to purchaseOrderId: {}", command.coupon, command.id)
        return command.id
    }

}
