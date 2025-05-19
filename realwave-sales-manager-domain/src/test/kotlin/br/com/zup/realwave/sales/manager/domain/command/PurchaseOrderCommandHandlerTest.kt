package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCommandHandler
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class PurchaseOrderCommandHandlerTest {

    val purchaseOrderValidator = mockk<PurchaseOrderValidator>()

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)
    val purchaseOrderProducer = mockk<PurchaseOrderProducer>()

    val commandHandler = PurchaseOrderCommandHandler(purchaseOrderValidator, purchaseOrderProducer).apply {
        this.repositoryManager = repository
    }

    init {
        every { purchaseOrderValidator.validate(any()) } returns true
        every { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(any()) } returns Unit
    }

    @Test
    fun testCreatePurchaseOrder() {
        val command = buildCreatePurchaseOrderCommand()

        val purchaseOrder = commandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        verify { purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder) }

        assertEquals(expected = command.id, actual = purchaseOrder.id)
        assertEquals(expected = command.purchaseOrderType, actual = purchaseOrder.type)
        assertEquals(expected = command.callback, actual = purchaseOrder.callback)
    }

    @Test
    fun testUpdatePurchaseOrderType() {
        val purchaseOrder = buildPurchaseOrder()
        val purchaseOrderType = PurchaseOrderType.BUY
        val command = buildUpdatePurchaseOrderTypeCommand(purchaseOrder, purchaseOrderType)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        commandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = purchaseOrderType, actual = purchaseOrder.type)
    }

    @Test
    fun testDeletePurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder()
        val command = buildDeletePurchaseOrderCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        commandHandler.handle(command)

        assertEquals(expected = PurchaseOrderStatus.DELETED, actual = purchaseOrder.status)
    }

    @Test
    fun testValidatePurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder()
        val command = buildValidatePurchaseOrderCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        commandHandler.handle(command)

        verify { purchaseOrderValidator.validate(purchaseOrder) }

    }

    @Test(expected = NotFoundException::class)
    fun testPurchaseOrderNotFound() {
        val purchaseOrder = buildPurchaseOrder()
        val command = buildUpdatePurchaseOrderTypeCommand(purchaseOrder)

        every { repository.get(purchaseOrder.id) } throws br.com.zup.eventsourcing.core.Repository.NotFoundException()

        commandHandler.handle(command)
    }

    @Test
    fun testFindPurchaseOrder(){
        val purchaseOrder = buildPurchaseOrder()
        val command = builFindPurchaseOrderCommand(purchaseOrder.id.value)

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        val purchaseReturn = commandHandler.handle(command)

        assertEquals(expected = purchaseOrder.idAsString(), actual = purchaseReturn.idAsString())
    }

    @Test(expected = NotFoundException::class)
    fun testFindPurchaseOrderWithError(){
        val purchaseOrder = buildPurchaseOrder()
        val command = builFindPurchaseOrderCommand(purchaseOrder.id.value)

        every { repository.get(purchaseOrder.id) }  throws NotFoundException()
        val purchaseReturn = commandHandler.handle(command)
    }



    private fun buildCreatePurchaseOrderCommand(
        purchaseOrderType: PurchaseOrderType = PurchaseOrderType.JOIN,
        callback: Callback? = Callback(url = "http://callback", headers = null)
    ) = CreatePurchaseOrderCommand(
        purchaseOrderType = purchaseOrderType,
        callback = callback
    )

    private fun buildUpdatePurchaseOrderTypeCommand(
        purchaseOrder: PurchaseOrder,
        type: PurchaseOrderType = PurchaseOrderType.JOIN
    ) = UpdatePurchaseOrderType(
        id = PurchaseOrderId(purchaseOrder.idAsString()),
        type = type
    )

    private fun buildDeletePurchaseOrderCommand(
        purchaseOrder: PurchaseOrder
    ) = DeletePurchaseOrderCommand(
        id = PurchaseOrderId(purchaseOrder.idAsString())
    )

    private fun buildValidatePurchaseOrderCommand(
        purchaseOrder: PurchaseOrder
    ) = ValidatePurchaseOrder(
        id = PurchaseOrderId(purchaseOrder.idAsString())
    )

    private fun builFindPurchaseOrderCommand(idPurchase: String) = FindPurchaseOrderCommand(
        id = PurchaseOrderId(idPurchase)
    )

}
