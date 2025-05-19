package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCustomerOrderCommandHandler
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckout
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckoutResolver
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderCustomerOrderCommandHandlerTest {

    val purchaseOrderValidator = mockk<PurchaseOrderValidator>()
    val purchaseOrderCheckout = mockk<PurchaseOrderCheckout>()
    val purchaseOrderCheckoutFactory = mockk<PurchaseOrderCheckoutResolver>()
    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)
    val purchaseOrderProducer = mockk<PurchaseOrderProducer>()

    val customerOrderCommandHandler = PurchaseOrderCustomerOrderCommandHandler(
        purchaseOrderValidator,
        purchaseOrderCheckoutFactory,
        purchaseOrderProducer
    ).apply {
        this.repositoryManager = repository
    }

    init {
        every { purchaseOrderCheckoutFactory.resolve(any()) } returns purchaseOrderCheckout
        every { purchaseOrderValidator.validate(any()) } returns true
        every { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(any()) } returns Unit
    }

    @Test
    fun testCheckout() {
        val purchaseOrder = buildPurchaseOrder()
        val command = buildCheckoutCommand(purchaseOrder)
        val channel = command.channel
        val securityCodes = command.securityCodes
        val customerOrder = buildCustomerOrder()

        every { repository.get(purchaseOrder.id) } returns purchaseOrder
        every { purchaseOrderCheckout.checkout(any(), any(), any()) } returns customerOrder

        customerOrderCommandHandler.handle(command)

        verify { purchaseOrderValidator.validate(purchaseOrder) }
        verify {
            purchaseOrderCheckout.checkout(
                purchaseOrder = purchaseOrder,
                channel = channel,
                securityCode = securityCodes
            )
        }
        verify { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder) }

        assertEquals(expected = PurchaseOrderStatus.CHECKED_OUT, actual = purchaseOrder.status)
        assertEquals(expected = customerOrder, actual = purchaseOrder.customerOrder)
        assertEquals(expected = channel, actual = purchaseOrder.channelCheckout)

        securityCodes.forEach { securityCode ->
            assertTrue(purchaseOrder.securityCodeInformed!!.any { it.methodId == securityCode.methodId })
            assertTrue(purchaseOrder.securityCodeInformed!!.first { it.methodId == securityCode.methodId }.securityCodeInformed)
        }

    }

    @Test(expected = BusinessException::class)
    fun testCheckoutWithInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        val command = buildCheckoutCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerOrderCommandHandler.handle(command)
    }

    @Test(expected = BusinessException::class)
    fun testCheckoutWithoutCustomer() {
        val purchaseOrder = buildPurchaseOrder()
        purchaseOrder.customer = null
        val command = buildCheckoutCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerOrderCommandHandler.handle(command)
    }

    @Test(expected = BusinessException::class)
    fun testCheckoutWithoutItems() {
        val purchaseOrder = buildPurchaseOrder(items = listOf())
        val command = buildCheckoutCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerOrderCommandHandler.handle(command)
    }

    @Test(expected = BusinessException::class)
    fun testCheckoutWithoutPayment() {
        val purchaseOrder = PurchaseOrder(PurchaseOrderId(), PurchaseOrderType.JOIN)
        val command = buildCheckoutCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerOrderCommandHandler.handle(command)
    }

    @Test
    fun testUpdateCustomerOrder() {
        val purchaseOrder = buildPurchaseOrder()
            .apply { callback = Callback("callback", null) }

        val customerOrder = buildCustomerOrder(status = "completed")
        val command = buildUpdateCustomerOrderCommand(purchaseOrder, customerOrder)
        val reason = command.reason

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        customerOrderCommandHandler.handle(command);

        assertEquals(expected = customerOrder, actual = purchaseOrder.customerOrder)
        assertEquals(expected = reason, actual = purchaseOrder.reason)
        assertEquals(expected = PurchaseOrderStatus.COMPLETED, actual = purchaseOrder.status)

        verify { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder) }

    }

    private fun buildCheckoutCommand(
        purchaseOrder: PurchaseOrder,
        channel: Channel = Channel("channel"),
        securityCodes: List<SecurityCode> = listOf(
            SecurityCode(
                methodId = "card-id",
                securityCode = "123"
            )
        )
    ) = CheckoutCommand(
        id = PurchaseOrderId(purchaseOrder.idAsString()),
        channel = channel,
        securityCodes = securityCodes
    )

    private fun buildUpdateCustomerOrderCommand(
        purchaseOrder: PurchaseOrder,
        customerOrder: CustomerOrder,
        reason: Reason? = Reason("code", "description")
    ) = UpdateCustomerOrderCommand(
        id = PurchaseOrderId(purchaseOrder.idAsString()),
        customerOrder = customerOrder,
        reason = reason
    )

    private fun buildCustomerOrder(
        customerOrderId: String = "customeOrderId",
        status: String = "completed",
        steps: List<Step> = listOf()
    ) = CustomerOrder(
        customerOrderId = customerOrderId,
        status = status,
        steps = steps
    )

}
