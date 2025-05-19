package br.com.zup.test.realwave.sales.manager.query.repository

import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.repository.CustomerOrderRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.test.realwave.sales.manager.query.repository.config.RepositoryBaseTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

/**
 * Created by branquinho on 12/07/17.
 */
class CustomerOrderRepositoryTest : RepositoryBaseTest() {

    @Autowired
    private lateinit var purchaseOrderRepository: PurchaseOrderRepository

    @Autowired
    private lateinit var customerOrderRepository: CustomerOrderRepository

    @Test
    fun findOneTest() {
        val purchaseOrderId = PurchaseOrderId()
        val rowsPurchaseOrderSaved = purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            type = PurchaseOrderType.JOIN,
            channelCreate = Channel("APP"),
            callback = null,
            customer = null,
            version = 0
        )

        assertEquals(1, rowsPurchaseOrderSaved, "Purchase Order not created")

        val newCustomerOrder = getCustomerOrder()
        val rowsCustomerOrderSaved = customerOrderRepository.saveCustomerOrder(
            purchaseOrderId = purchaseOrderId,
            customerOrder = newCustomerOrder
        )

        assertEquals(1, rowsCustomerOrderSaved, "CustomerOrder saved")

        val customerOrder = customerOrderRepository.findOne(purchaseOrderId)

        assertEquals(newCustomerOrder, customerOrder, "Customer Order ${purchaseOrderId.value} not founded")
    }

    @Test
    fun savePurchaseOrderTest() {
        val purchaseOrderId = PurchaseOrderId()
        val rowsPurchaseOrderSaved = purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            type = PurchaseOrderType.JOIN,
            channelCreate = Channel("APP"),
            callback = null,
            customer = null,
            version = 0
        )

        assertEquals(1, rowsPurchaseOrderSaved, "Purchase Order not created")

        val customerOrder = getCustomerOrder()
        val rowsCustomerOrderSaved =
            customerOrderRepository.saveCustomerOrder(purchaseOrderId = purchaseOrderId, customerOrder = customerOrder)

        assertEquals(1, rowsCustomerOrderSaved, "CustomerOrder saved")
    }

    private fun getCustomerOrder(): CustomerOrder {
        val step = listOf(
            Step(
                step = "ACTIVATION",
                status = "PENDING",
                startedAt = "2017-07-20T13:59:59+000:00",
                endedAt = "2017-07-26T13:59:59+000:00",
                processed = 0,
                total = 1
            )
        )

        return CustomerOrder(customerOrderId = "1302A002-FD39-4A2E-9AE0-72A9F177A812", status = "PENDING", steps = step)
    }

}
