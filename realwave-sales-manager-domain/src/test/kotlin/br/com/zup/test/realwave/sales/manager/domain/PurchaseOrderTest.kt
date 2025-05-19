package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.*
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderTypeUpdated
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderValidationMethodsException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

/**
 * Created by luizs on 30/05/2017
 */
class PurchaseOrderTest {

    @Test
    fun create() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)

        assertNotNull(purchaseOrder)
        assertTrue(purchaseOrder.event is PurchaseOrderCreated)
    }

    @Test
    fun createWithCallback() {
        val purchaseOrder = PurchaseOrder(
            PurchaseOrderId(), PurchaseOrderType.JOIN,
            Callback("http://localhost:8080/callback", null)
        )

        assertNotNull(purchaseOrder)
        assertTrue(purchaseOrder.event is PurchaseOrderCreated)
    }

    @Test(expected = BusinessException::class)
    fun purchaseOrderWithoutCustomerTest() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        purchaseOrder.customer = null
        purchaseOrder.canCheckout()
    }

    @Test(expected = BusinessException::class)
    fun purchaseOrderWithoutPaymentMeanTest() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        purchaseOrder.payment = Payment()
        purchaseOrder.canCheckout()
    }

    @Test(expected = BusinessException::class)
    fun purchaseOrderWithoutPaymentMeanWIthNoPaymentTest() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.payment = Payment()
        purchaseOrder.canCheckout()
    }

    @Test
    fun validatePurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        val purchaseOrderValidator = Mockito.mock(PurchaseOrderValidator::class.java)

        purchaseOrder.validatePurchaseOrder(purchaseOrderValidator)
        Mockito.verify(purchaseOrderValidator).validate(purchaseOrder)
    }

    @Test
    fun validatePurchaseOrderClosed() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN, status = PurchaseOrderStatus.COMPLETED)
        val purchaseOrderValidator = Mockito.mock(PurchaseOrderValidator::class.java)

        purchaseOrder.validatePurchaseOrder(purchaseOrderValidator)
        Mockito.verify(purchaseOrderValidator).validate(purchaseOrder)
    }

    @Test
    fun updatePurchaseOrderType() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.updatePurchaseOrderType(PurchaseOrderType.CHANGE)

        assertEquals(PurchaseOrderType.CHANGE, purchaseOrder.type)
        assertTrue(purchaseOrder.event is PurchaseOrderTypeUpdated)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotUpdatePurchaseOrderType() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.BUY)
        purchaseOrder.updatePurchaseOrderType(PurchaseOrderType.JOIN)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotUpdatePurchaseOrderTypeWithInvalidStatus() {
        val purchaseOrder = buildPurchaseOrder(type = null, status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updatePurchaseOrderType(PurchaseOrderType.CHANGE)
    }

    @Test
    fun createWithTypeOffersActivationType() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithProductId()
                )
            ),
            type = PurchaseOrderType.BUY
        )
        assertEquals(PurchaseOrderType.BUY, purchaseOrder.type)
    }

    @Test
    fun createWithTypeJoin() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offersItems()
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        assertEquals(PurchaseOrderType.JOIN, purchaseOrder.type)
    }

    @Test
    fun createWithTypeChangeWithSubscription() {
        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = PurchaseOrder(aggregateId = purchaseOrderId, purchaseOrderType = PurchaseOrderType.CHANGE)

        assertEquals(PurchaseOrderType.CHANGE, purchaseOrder.type)
    }

    @Test
    fun deletePurchaseOrder() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)

        purchaseOrder.deletePurchaseOrder()

        assertEquals(expected = PurchaseOrderStatus.DELETED, actual = purchaseOrder.status)
    }

    @Test(expected = BusinessException::class)
    fun shouldNotDeletePurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN, status = PurchaseOrderStatus.COMPLETED)

        purchaseOrder.deletePurchaseOrder()

        assertEquals(expected = PurchaseOrderStatus.COMPLETED, actual = purchaseOrder.status)
    }

    @Test(expected = PurchaseOrder.InvalidEvent::class)
    fun `invalid event type should throw InvalidEvent exception`() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        purchaseOrder.applyEvent(Event())
    }

    @Test(expected = PurchaseOrderValidationMethodsException::class)
    fun `Methods repeated  in payment  should throw PurchaseOrverValidationMethods exception`() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.payment = getPaymentMethodsRepeated()
        purchaseOrder.canCheckout()

    }

    private fun getPaymentMethodsRepeated(): Payment {

        val paymentRequestList =
            Payment(
                methods = listOf(
                    Payment.PaymentMethod(
                        method = "BOLETO",
                        methodId = null,
                        installments = 0,
                        price = Price("10", 10, 1),
                        customFields = null
                    ),
                    Payment.PaymentMethod(
                        method = "BOLETO",
                        methodId = null,
                        installments = 0,
                        price = Price("10", 10, 1),
                        customFields = null
                    )

                ), description = null
            )
        return paymentRequestList
    }


}
