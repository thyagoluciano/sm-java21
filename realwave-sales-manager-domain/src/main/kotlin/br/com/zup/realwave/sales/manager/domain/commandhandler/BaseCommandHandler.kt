package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.common.exception.handler.to.ResourceValue
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseCommandHandler {

    protected val log = LogManager.getLogger(this.javaClass)

    @Autowired
    lateinit var repositoryManager: Repository<PurchaseOrder>

    protected fun withPurchaseOrder(
        purchaseOrderId: PurchaseOrderId,
        doOnPurchaseOrder: (PurchaseOrder) -> Unit
    ): PurchaseOrder {
        log.info("Begin get purchase order in eventstore")
        val purchaseOrder = getPurchaseOrder(purchaseOrderId)
        log.info("End get purchase order in eventstore")
        doOnPurchaseOrder(purchaseOrder)
        log.info("Begin save purchase order events in eventstore")
        repositoryManager.save(purchaseOrder)
        log.info("End save purchase order events in eventstore")
        return purchaseOrder
    }

    protected fun getPurchaseOrder(id: PurchaseOrderId): PurchaseOrder {
        try {
            log.debug("Finding purchase order to id: {}", id)
            val purchaseOrder = repositoryManager.get(id)
            log.debug("Found purchase order [{}]", purchaseOrder)
            return purchaseOrder
        } catch (e: br.com.zup.eventsourcing.core.Repository.NotFoundException) {
            log.error("Purchase order not found to id: {} in tenant: [{}]")
            throw NotFoundException(ResourceValue(PurchaseOrderId::class.java, id.value))
        }
    }

}
