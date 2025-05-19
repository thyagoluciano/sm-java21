package br.com.zup.realwave.sales.manager.command.application.controller

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.sales.manager.api.PurchaseOrderCommandApi
import br.com.zup.realwave.sales.manager.api.request.CheckoutRequest
import br.com.zup.realwave.sales.manager.api.request.CouponRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerOrderCallbackRequest
import br.com.zup.realwave.sales.manager.api.request.CustomerRequest
import br.com.zup.realwave.sales.manager.api.request.FreightRequest
import br.com.zup.realwave.sales.manager.api.request.InstallationAttributesRequest
import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.MgmRequest
import br.com.zup.realwave.sales.manager.api.request.OnBoardingSaleRequest
import br.com.zup.realwave.sales.manager.api.request.PaymentRequest
import br.com.zup.realwave.sales.manager.api.request.ProtocolRequest
import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderCouponRequest
import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderRequest
import br.com.zup.realwave.sales.manager.api.request.SalesForceRequest
import br.com.zup.realwave.sales.manager.api.request.SubscriptionRequest
import br.com.zup.realwave.sales.manager.api.response.CheckoutResponse
import br.com.zup.realwave.sales.manager.api.response.CreatePurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.CustomerOrder
import br.com.zup.realwave.sales.manager.api.response.DeleteInstallationAttributesResponse
import br.com.zup.realwave.sales.manager.api.response.DeleteResponse
import br.com.zup.realwave.sales.manager.api.response.ProtocolResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderItemResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderMgmResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderSalesForceResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderTypeResponse
import br.com.zup.realwave.sales.manager.api.response.SegmentationResponse
import br.com.zup.realwave.sales.manager.api.response.SubscriptionResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateCouponResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateCustomerIdResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateFreightResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateInstallationAttributesResponse
import br.com.zup.realwave.sales.manager.api.response.UpdateOnBoardingSaleResponse
import br.com.zup.realwave.sales.manager.api.response.UpdatePaymentResponse
import br.com.zup.realwave.sales.manager.api.response.ValidateResponse
import br.com.zup.realwave.sales.manager.command.application.helper.PurchaseOrderHelper
import br.com.zup.realwave.sales.manager.command.application.helper.mapToResponse
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.command.AddItemCommand
import br.com.zup.realwave.sales.manager.domain.command.CheckoutCommand
import br.com.zup.realwave.sales.manager.domain.command.CreatePurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.CreatePurchaseOrderCouponCommand
import br.com.zup.realwave.sales.manager.domain.command.DeleteInstallationAttributesCommand
import br.com.zup.realwave.sales.manager.domain.command.DeleteMgmCommand
import br.com.zup.realwave.sales.manager.domain.command.DeletePurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.FindPurchaseOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.RemoveItemCommand
import br.com.zup.realwave.sales.manager.domain.command.RemoveSalesForceCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateCouponCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateCustomerCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateCustomerOrderCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateFreightCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateInstallationAttributesCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateItemCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateMgmCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateOnBoardingSaleCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdatePaymentCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateProtocolCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdatePurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.command.UpdateSalesForceCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateSegmentationCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateSubscriptionCommand
import br.com.zup.realwave.sales.manager.domain.command.ValidatePurchaseOrder
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCouponCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCustomerCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderCustomerOrderCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderFreightCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderInstallationAttributesCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderItemCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderMgmCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderOnBoardingSaleCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderPaymentCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderProtocolCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderSalesForceCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderSegmentationCommandHandler
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderSubscriptionCommandHandler
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderValidationException
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class PurchaseOrderCommandController(
    private val commandHandler: PurchaseOrderCommandHandler,
    private val couponCommandHandler: PurchaseOrderCouponCommandHandler,
    private val segmentationCommandHandler: PurchaseOrderSegmentationCommandHandler,
    private val onBoardingSaleCommandHandler: PurchaseOrderOnBoardingSaleCommandHandler,
    private val mgmCommandHandler: PurchaseOrderMgmCommandHandler,
    private val customerCommandHandler: PurchaseOrderCustomerCommandHandler,
    private val itemCommandHandler: PurchaseOrderItemCommandHandler,
    private val paymentCommandHandler: PurchaseOrderPaymentCommandHandler,
    private val installationAttributesCommandHandler: PurchaseOrderInstallationAttributesCommandHandler,
    private val customerOrderCommandHandler: PurchaseOrderCustomerOrderCommandHandler,
    private val subscriptionCommandHandler: PurchaseOrderSubscriptionCommandHandler,
    private val protocolCommandHandler: PurchaseOrderProtocolCommandHandler,
    private val salesForceCommandHandler: PurchaseOrderSalesForceCommandHandler,
    private val freightCommandHandler: PurchaseOrderFreightCommandHandler
) : PurchaseOrderCommandApi {

    override fun create(
        @Valid @RequestBody(required = false) purchaseOrderRequest: PurchaseOrderRequest?
    ): CreatePurchaseOrderResponse {

        val purchaseOrderType =
            if (purchaseOrderRequest?.type == null) null else PurchaseOrderType.valueOf(purchaseOrderRequest.type!!)

        val command = CreatePurchaseOrderCommand(
            purchaseOrderType = purchaseOrderType,
            customer = if (purchaseOrderRequest?.customer == null) null else Customer(purchaseOrderRequest.customer!!),
            callback = PurchaseOrderHelper.callback(purchaseOrderRequest?.callback)
        )

        val purchaseOrder = commandHandler.handle(command = command)
        return CreatePurchaseOrderResponse(id = purchaseOrder.id.value)
    }

    override fun findByPurchaseOrderId(
            @PathVariable("purchaseOrderId") purchaseOrderId: String
    ): PurchaseOrderResponse {

        val command = FindPurchaseOrderCommand(id = PurchaseOrderId(purchaseOrderId))
        val purchaseOrder = commandHandler.handle(command = command)
        return purchaseOrder.mapToResponse()
    }

    override fun createPurchaseCoupon(
        @Valid @RequestBody purchaseOrderRequest: PurchaseOrderCouponRequest
    ): CreatePurchaseOrderResponse {

        val command = CreatePurchaseOrderCouponCommand(
            couponCode = CouponCode(purchaseOrderRequest.couponCode.toString()),
            customer = Customer(purchaseOrderRequest.customerId!!),
            productId = ProductId(purchaseOrderRequest.productId!!),
            callback = PurchaseOrderHelper.callback(purchaseOrderRequest.callback)
        )

        val purchaseOrder = couponCommandHandler.handle(command = command)
        return CreatePurchaseOrderResponse(id = purchaseOrder.idAsString())
    }

    override fun segmentation(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody segmentation: JsonNode
    ): SegmentationResponse {
        val command = UpdateSegmentationCommand(
            id = PurchaseOrderId(purchaseOrderId),
            segmentation = Segmentation(segmentation)
        )

        segmentationCommandHandler.handle(command = command)
        return SegmentationResponse(purchaseOrderId = purchaseOrderId, segmentation = segmentation)
    }

    override fun updateOnBoardingSale(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody onBoardingSale: OnBoardingSaleRequest
    ): UpdateOnBoardingSaleResponse {
        val command = UpdateOnBoardingSaleCommand(
            id = PurchaseOrderId(purchaseOrderId),
            onBoardingSale = OnBoardingSale(
                offer = CatalogOfferId(onBoardingSale.id!!),
                customFields = onBoardingSale.customFields
            )
        )

        onBoardingSaleCommandHandler.handle(command = command)

        return UpdateOnBoardingSaleResponse(
            purchaseOrderId = purchaseOrderId,
            id = onBoardingSale.id!!,
            customFields = onBoardingSale.customFields
        )
    }

    override fun updateMgm(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody mgmRequest: MgmRequest
    ): PurchaseOrderMgmResponse {

        val command = UpdateMgmCommand(
            id = PurchaseOrderId(purchaseOrderId),
            mgm = Mgm(
                code = mgmRequest.code!!,
                customFields = mgmRequest.customFields
            )
        )

        mgmCommandHandler.handle(command = command)
        return PurchaseOrderMgmResponse(purchaseOrderId = purchaseOrderId, code = mgmRequest.code)
    }

    override fun deleteMgm(@PathVariable("purchaseOrderId") purchaseOrderId: String): PurchaseOrderMgmResponse {
        val command = DeleteMgmCommand(
            id = PurchaseOrderId(purchaseOrderId)
        )

        mgmCommandHandler.handle(command = command)
        return PurchaseOrderMgmResponse(purchaseOrderId = purchaseOrderId, code = null)
    }

    override fun updateCustomerId(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody customerRequest: CustomerRequest
    ): UpdateCustomerIdResponse {

        val command = UpdateCustomerCommand(
            id = PurchaseOrderId(purchaseOrderId),
            customer = Customer(customerRequest.customer!!)
        )

        customerCommandHandler.handle(command = command)
        return UpdateCustomerIdResponse(purchaseOrderId = purchaseOrderId, customer = customerRequest.customer)
    }

    override fun addItem(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody itemRequest: ItemRequest
    ): PurchaseOrderItemResponse {

        val command = AddItemCommand(
            id = PurchaseOrderId(purchaseOrderId),
            item = PurchaseOrderHelper.fromItem(itemRequest)
        )

        val itemId = itemCommandHandler.handle(command = command)
        return PurchaseOrderItemResponse(purchaseOrderId = purchaseOrderId, itemId = itemId.value)
    }

    override fun updateItem(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @PathVariable("itemId") itemId: String,
        @Valid @RequestBody itemRequest: ItemRequest
    ): PurchaseOrderItemResponse {

        val command = UpdateItemCommand(
            id = PurchaseOrderId(purchaseOrderId),
            item = PurchaseOrderHelper.fromItem(itemRequest, Item.Id(itemId))
        )

        itemCommandHandler.handle(command = command)
        return PurchaseOrderItemResponse(purchaseOrderId = purchaseOrderId, itemId = itemId)
    }

    override fun deleteItem(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @PathVariable("catalogOfferId") itemId: String
    ): PurchaseOrderItemResponse {

        val command = RemoveItemCommand(
            id = PurchaseOrderId(purchaseOrderId),
            itemId = Item.Id(itemId)
        )

        itemCommandHandler.handle(command = command)
        return PurchaseOrderItemResponse(purchaseOrderId = purchaseOrderId, itemId = itemId)
    }

    override fun updatePayment(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody paymentRequest: PaymentRequest
    ): UpdatePaymentResponse {

        PurchaseOrderHelper.validatePaymentRequestList(paymentRequestList = paymentRequest)

        var description: Payment.Description? = null
        if (paymentRequest.description != null) {
            description = Payment.Description(paymentRequest.description!!)
        }
        val command = UpdatePaymentCommand(
            id = PurchaseOrderId(purchaseOrderId),
            payment = Payment(
                description = description,
                methods = paymentRequest.methods.map {
                    Payment.PaymentMethod(
                        method = it.method!!,
                        methodId = it.methodId,
                        installments = it.installments,
                        price = if (it.price != null) PurchaseOrderHelper.fromPrice(it.price!!) else null,
                        customFields = it.customFields
                    )
                },
                async = paymentRequest.async
            )
        )

        paymentCommandHandler.handle(command = command)
        return UpdatePaymentResponse(purchaseOrderId = purchaseOrderId)
    }

    override fun updateFreight(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody freightRequest: FreightRequest
    ): UpdateFreightResponse {

        val command = UpdateFreightCommand(
            id = PurchaseOrderId(purchaseOrderId),
            freight = Freight(
                type = Freight.Type(freightRequest.type!!),
                price = Price(
                    amount = freightRequest.price.amount!!,
                    currency = freightRequest.price.currency!!,
                    scale = freightRequest.price.scale!!
                ),
                address = Freight.Address(
                    city = freightRequest.address.city!!,
                    state = freightRequest.address.state!!,
                    name = freightRequest.address.name!!,
                    complement = freightRequest.address.complement,
                    country = freightRequest.address.country!!,
                    district = freightRequest.address.district!!,
                    street = freightRequest.address.street!!,
                    zipCode = freightRequest.address.zipCode!!,
                    number = freightRequest.address.number!!
                ),
                deliveryTotalTime = Freight.DeliveryTotalTime(freightRequest.deliveryTotalTime!!)
            )
        )

        freightCommandHandler.handle(command = command)

        return UpdateFreightResponse(purchaseOrderId = purchaseOrderId)
    }

    override fun updateInstallationAttributes(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody request: InstallationAttributesRequest
    ): UpdateInstallationAttributesResponse {

        val command = UpdateInstallationAttributesCommand(
            id = PurchaseOrderId(purchaseOrderId),
            installationAttribute = InstallationAttribute(
                productTypeId = ProductTypeId(request.productTypeId!!),
                attributes = request.attributes!!
            )
        )

        installationAttributesCommandHandler.handle(command = command)

        return UpdateInstallationAttributesResponse(
            purchaseOrderId = purchaseOrderId,
            productTypeId = request.productTypeId,
            attributes = request.attributes
        )
    }

    override fun deleteInstallationAttributes(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @PathVariable("productTypeId") productTypeId: String
    ): DeleteInstallationAttributesResponse {

        val command = DeleteInstallationAttributesCommand(
            id = PurchaseOrderId(purchaseOrderId),
            productTypeId = ProductTypeId(productTypeId)
        )

        installationAttributesCommandHandler.handle(command = command)
        return DeleteInstallationAttributesResponse(purchaseOrderId = purchaseOrderId, productTypeId = productTypeId)
    }

    override fun updateCoupon(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody couponRequest: CouponRequest
    ): UpdateCouponResponse {

        val command = UpdateCouponCommand(
            id = PurchaseOrderId(purchaseOrderId),
            coupon = CouponCode(
                code = couponRequest.code!!,
                customFields = couponRequest.customFields
            )
        )

        couponCommandHandler.handle(command = command)
        return UpdateCouponResponse(
            purchaseOrderId = purchaseOrderId,
            code = couponRequest.code,
            customFields = couponRequest.customFields
        )
    }

    override fun validate(@PathVariable("purchaseOrderId") purchaseOrderId: String): ValidateResponse {
        val command = ValidatePurchaseOrder(id = PurchaseOrderId(purchaseOrderId))
        commandHandler.handle(command = command)
        return ValidateResponse(purchaseOrderId = purchaseOrderId)
    }

    override fun checkout(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody(required = false) checkoutRequest: CheckoutRequest?
    ): CheckoutResponse {
        val context = RealwaveContextHolder.getContext()
        val channel: Channel = if (null != context?.channel) Channel(context.channel) else Channel("NOT INFORMED")

        return try {
            val command = CheckoutCommand(
                id = PurchaseOrderId(purchaseOrderId),
                channel = channel,
                securityCodes = checkoutRequest?.let {
                    it.paymentSecurityCodes?.map {
                        SecurityCode(
                            methodId = it.methodId!!,
                            securityCode = it.securityCode!!
                        )
                    }
                } ?: emptyList()
            )
            val customerOrder = customerOrderCommandHandler.handle(command)

            return CheckoutResponse(
                purchaseOrderId,
                CustomerOrder(
                    customerOrder.customerOrderId!!,
                    PurchaseOrderHelper.toBoletoResponse(customerOrder.boleto)
                )
            )
        } catch (e: PurchaseOrderValidationException) {
            CheckoutResponse(purchaseOrderId, null)
        }
    }

    override fun delete(@PathVariable("purchaseOrderId") purchaseOrderId: String): DeleteResponse {
        val command = DeletePurchaseOrderCommand(id = PurchaseOrderId(purchaseOrderId))
        commandHandler.handle(command)
        return DeleteResponse(purchaseOrderId = purchaseOrderId)
    }

    override fun callback(@RequestBody @Valid customerOrderCallbackRequest: CustomerOrderCallbackRequest) {
        val command = UpdateCustomerOrderCommand(
            id = PurchaseOrderId(customerOrderCallbackRequest.externalId!!),
            customerOrder = PurchaseOrderHelper.fromCustomerOrder(
                id = customerOrderCallbackRequest.id!!,
                status = customerOrderCallbackRequest.status!!,
                steps = customerOrderCallbackRequest.steps!!.map {
                    Step(
                        step = it.step,
                        status = it.status,
                        startedAt = it.startedAt,
                        endedAt = it.startedAt,
                        processed = it.processed,
                        total = it.total
                    )
                }
            ),
            reason = if (customerOrderCallbackRequest.reason != null)
                PurchaseOrderHelper.fromReason(customerOrderCallbackRequest.reason!!)
            else null
        )

        customerOrderCommandHandler.handle(command)
    }

    override fun protocol(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody protocolRequest: ProtocolRequest
    ): ProtocolResponse {

        val command = UpdateProtocolCommand(
            id = PurchaseOrderId(purchaseOrderId),
            protocol = Protocol(protocolRequest.protocol)
        )

        protocolCommandHandler.handle(command)
        return ProtocolResponse(purchaseOrderId = purchaseOrderId, protocol = protocolRequest.protocol)
    }

    override fun subscription(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody subscriptionRequest: SubscriptionRequest
    ): SubscriptionResponse {

        val command = UpdateSubscriptionCommand(
            id = PurchaseOrderId(purchaseOrderId),
            subscriptionId = Subscription(subscriptionRequest.id)
        )

        subscriptionCommandHandler.handle(command)
        return SubscriptionResponse(purchaseOrderId = purchaseOrderId, id = subscriptionRequest.id)
    }

    override fun purchaseOrderType(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody(required = true) purchaseOrderRequest: PurchaseOrderRequest
    ): PurchaseOrderTypeResponse {
        val command = UpdatePurchaseOrderType(
            id = PurchaseOrderId(purchaseOrderId),
            type = PurchaseOrderType.valueOf(purchaseOrderRequest.type!!)
        )

        commandHandler.handle(command)
        return PurchaseOrderTypeResponse(purchaseOrderId = purchaseOrderId, type = purchaseOrderRequest.type!!)
    }

    override fun updateSalesForce(
        @PathVariable("purchaseOrderId") purchaseOrderId: String,
        @Valid @RequestBody salesForceRequest: SalesForceRequest
    ): PurchaseOrderSalesForceResponse {

        val command = UpdateSalesForceCommand(
            id = PurchaseOrderId(purchaseOrderId),
            salesForce = SalesForce(
                id = salesForceRequest.id,
                name = salesForceRequest.name
            )
        )

        salesForceCommandHandler.handle(command = command)
        return PurchaseOrderSalesForceResponse(purchaseOrderId = purchaseOrderId, salesForceId = salesForceRequest.id)
    }

    override fun deleteSalesForce(
        @PathVariable("purchaseOrderId") purchaseOrderId: String
    ): PurchaseOrderSalesForceResponse {

        val command = RemoveSalesForceCommand(
            id = PurchaseOrderId(purchaseOrderId)
        )

        salesForceCommandHandler.handle(command = command)
        return PurchaseOrderSalesForceResponse(purchaseOrderId = purchaseOrderId)
    }

}
