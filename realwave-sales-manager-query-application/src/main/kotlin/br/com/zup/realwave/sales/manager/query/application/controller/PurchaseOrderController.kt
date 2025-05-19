package br.com.zup.realwave.sales.manager.query.application.controller

import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.common.exception.handler.to.ResourceValue
import br.com.zup.realwave.sales.manager.api.PurchaseOrderQueryApi
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderStatusResponse
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.query.application.controller.helpers.mapToResponse
import br.com.zup.realwave.sales.manager.query.application.controller.helpers.mapToStatusResponse
import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by branquinho on 05/06/17.
 */
@RestController
class PurchaseOrderController(
    private val purchaseOrderRepository: PurchaseOrderRepository
) : PurchaseOrderQueryApi {

    private val log = LogManager.getLogger(this.javaClass)

    override fun findByPurchaseOrderId(
        @PathVariable("purchaseOrderId") purchaseOrderId: String
    ): PurchaseOrderResponse? {
        log.debug("Received request to find purchase order for id: [{}].", purchaseOrderId)
        val purchaseOrder = purchaseOrderRepository.find(PurchaseOrderId(purchaseOrderId))
        val purchaseToObject = purchaseOrder?.mapToResponse()
        log.debug("Response created for purchaseOrderId {}. Response: [{}]", purchaseOrderId, purchaseOrder)
        return purchaseToObject ?: throw NotFoundException(ResourceValue("purchaseOrderId", purchaseOrderId))

    }

    override fun findByProtocol(
        @PathVariable("protocol") protocol: String
    ): PurchaseOrderResponse? {
        val purchaseOrder = purchaseOrderRepository.findByProtocol(Protocol(protocol))
        val purchaseToObject = purchaseOrder?.mapToResponse()
        return purchaseToObject ?: throw NotFoundException(ResourceValue("protocol", protocol))
    }

    override fun getPurchaseOrderStatus(
        @PathVariable("purchaseOrderId") purchaseOrderId: String
    ): PurchaseOrderStatusResponse? {
        log.debug("Received request to find purchase order for id: [{}].", purchaseOrderId)
        val purchaseOrder = purchaseOrderRepository.find(PurchaseOrderId(purchaseOrderId))
        log.debug("Response created for purchaseOrderId {}. Response: [{}]", purchaseOrderId, purchaseOrder)
        val purchaseStatusResponse = purchaseOrder?.mapToStatusResponse()
        return purchaseStatusResponse ?: throw NotFoundException(ResourceValue("purchaseOrderId", purchaseOrderId))
    }

    override fun findByCustomer(
        @RequestParam(value = "customerId", required = false) customerId: String,
        @RequestParam(value = "status", required = false) status: String?,
        @RequestParam(value = "start", required = false) start: String?,
        @RequestParam(value = "end", required = false) end: String?
    ): List<PurchaseOrderResponse>? {

        log.debug("Received request to find purchase order for customerId: [{}].", customerId)
        val purchaseOrder = validateSize(
            purchaseOrderRepository.find(
                customer = Customer(customerId),
                status = status,
                start = start,
                end = end
            )
        )
        log.debug("Response created for customerId {}. Response: [{}]", customerId, purchaseOrder)
        val purchaseToObject = purchaseOrder?.mapToResponse()
        return purchaseToObject ?: listOf()
    }

    private fun validateSize(purchaseOrders: List<PurchaseOrder>) =
        if (purchaseOrders.isNotEmpty()) purchaseOrders else null

}
