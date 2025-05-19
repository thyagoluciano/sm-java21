package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.Status
import br.com.zup.realwave.sales.manager.domain.updateCustomer
import org.junit.Assert
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderCustomerTest {

    @Test
    fun statusCustomerSucess() {
        val status = Status(Status.ACTIVE)
        assertEquals(expected = "ACTIVE", actual = status.name)
        Assert.assertNotNull(status)
    }

    @Test
    fun testUpdateCustomer() {
        val purchaseOrder = buildPurchaseOrder()
        val customer = Customer("customer-id")

        purchaseOrder.updateCustomer(customer)

        assertEquals(expected = customer, actual = purchaseOrder.customer)
    }

    @Test(expected = BusinessException::class)
    fun testUpdateCustomerOfInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updateCustomer(Customer("customer-id"))
    }

}
