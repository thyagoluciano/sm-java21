package br.com.zup.realwave.sales.manager.command.application.helper

import br.com.zup.realwave.sales.manager.api.response.CallbackResponse
import br.com.zup.realwave.sales.manager.api.response.Channel
import br.com.zup.realwave.sales.manager.api.response.CouponResponse
import br.com.zup.realwave.sales.manager.api.response.CustomerOrderResponse
import br.com.zup.realwave.sales.manager.api.response.CustomerResponse
import br.com.zup.realwave.sales.manager.api.response.Description
import br.com.zup.realwave.sales.manager.api.response.DiscountResponse
import br.com.zup.realwave.sales.manager.api.response.FreightResponse
import br.com.zup.realwave.sales.manager.api.response.InstallationAttributesResponse
import br.com.zup.realwave.sales.manager.api.response.ItemResponse
import br.com.zup.realwave.sales.manager.api.response.MgmResponse
import br.com.zup.realwave.sales.manager.api.response.OfferItemResponse
import br.com.zup.realwave.sales.manager.api.response.OfferValidity
import br.com.zup.realwave.sales.manager.api.response.OnBoardingSaleResponse
import br.com.zup.realwave.sales.manager.api.response.PaymentMethodResponse
import br.com.zup.realwave.sales.manager.api.response.PaymentResponse
import br.com.zup.realwave.sales.manager.api.response.Price
import br.com.zup.realwave.sales.manager.api.response.PricePerPeriodResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderResponse
import br.com.zup.realwave.sales.manager.api.response.PurchaseOrderStatusResponse
import br.com.zup.realwave.sales.manager.api.response.ReasonResponse
import br.com.zup.realwave.sales.manager.api.response.SalesForceResponse
import br.com.zup.realwave.sales.manager.api.response.StepResponse
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.discountPrice
import br.com.zup.realwave.sales.manager.domain.totalPrice
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson

fun PurchaseOrder.mapToResponse() =
        PurchaseOrderResponse(
                id = this.id.value,
                type = this.type?.name,
                segmentation = this.segmentation?.query,
                mgm = this.mgm?.mapToResponse(),
                salesForce = this.salesForce?.mapToResponse(),
                onBoardingSale = this.onBoardingSale?.mapToResponse(),
                customer = this.customer?.mapToResponse(),
                coupon = this.coupon?.mapToResponse(),
                totalPrice = toApiPrice(this.totalPrice()),
                discount = this.buildDiscount(),
                payment = this.payment.mapToResponse(),
                freight = this.freight?.mapToResponse(),
                status = this.status.mapToResponse(),
                items = this.items.mapToResponse(),
                installationAttributes = this.installationAttributes.mapToResponse(),
                protocol = this.protocol,
                subscriptionId = this.subscriptionId,
                channelCreate = Channel(this.channelCreate?.value),
                channelCheckout = Channel(this.channelCheckout?.value),
                callback = this.callback?.mapToResponse(),
                reason = this.reason?.mapToResponse(),
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
        )


fun PurchaseOrder.mapToStatusResponse() =
        PurchaseOrderStatusResponse(
                customerOrder = this.customerOrder?.mapToResponse(),
                status = this.status.mapToResponse()
        )

@JvmName("PurchsaseOrderListToPurchaseOrderResponseList")
fun List<PurchaseOrder>.mapToResponse() =
        this.map { p -> p.mapToResponse() }

private fun Mgm.mapToResponse() =
        MgmResponse(
                code = this.code,
                fields = this.customFields
        )

private fun SalesForce.mapToResponse() =
        SalesForceResponse(
                id = this.id,
                name = this.name
        )

private fun Payment.mapToResponse() =
        PaymentResponse(
                methods = this.methods.map { it.mapToResponse() },
                description = Description(this.description?.value)
        )

private fun Payment.PaymentMethod.mapToResponse() =
        PaymentMethodResponse(
                method = this.method,
                methodId = this.methodId,
                price = if (this.price != null) Price(
                        currency = this.price!!.currency,
                        amount = this.price!!.amount,
                        scale = this.price!!.scale
                ) else null,
                securityCodeInformed = this.securityCodeInformed,
                customFields = this.customFields,
                installments = this.installments
        )

private fun Freight.mapToResponse() = FreightResponse(
        type = this.type.value,
        price = FreightResponse.Price(
                amount = this.price.amount,
                currency = this.price.currency,
                scale = this.price.scale
        ),
        address = this.address.objectToJson().jsonToObject(FreightResponse.Address::class.java),
        deliveryTotalTime = this.deliveryTotalTime.value
)

private fun OnBoardingSale.mapToResponse() =
        OnBoardingSaleResponse(
                id = this.offer.value,
                fields = this.customFields
        )

private fun Customer.mapToResponse() =
        CustomerResponse(id = this.id)

private fun CustomerOrder.mapToResponse() =
        CustomerOrderResponse(
                customerOrderId = this.customerOrderId,
                status = this.status,
                steps = this.steps!!.map { it.mapToResponse() }
        )

private fun Step.mapToResponse() =
        StepResponse(
                step = this.step,
                status = this.status,
                startedAt = this.startedAt,
                endedAt = this.endedAt
        )

private fun CouponCode.mapToResponse() =
        CouponResponse(
                id = this.code,
                fields = this.customFields
        )

private fun PurchaseOrderStatus.mapToResponse() =
        this.name

private fun Item.mapToResponse() =
        ItemResponse(
                id = this.id.value,
                catalogOfferId = this.catalogOfferId.value,
                catalogOfferType = this.catalogOfferType.value,
                price = Price(
                        currency = this.price.currency,
                        amount = this.price.amount,
                        scale = this.price.scale
                ),
                validity = OfferValidity(
                        unlimited = this.validity.unlimited,
                        duration = this.validity.duration,
                        period = this.validity.period
                ),
                customFields = this.customFields.value,
                offerItems = this.offerItems.map { it.mapToResponse() },
                pricesPerPeriod = this.pricesPerPeriod.map { it.mapToResponse() },
                quantity = this.quantity.value
        )

private fun Callback.mapToResponse() =
        CallbackResponse(
                url = this.url,
                headers = this.headers
        )

private fun MutableSet<Item>.mapToResponse() =
        this.map { i -> i.mapToResponse() }

private fun Item.OfferItem.mapToResponse() =
        OfferItemResponse(
                productId = this.productId?.value,
                catalogOfferItemId = this.catalogOfferItemId.value,
                price = Price(
                        currency = this.price.currency,
                        amount = this.price.amount,
                        scale = this.price.scale
                ),
                customFields = this.customFields?.value,
                recurrent = this.recurrent,
                userParameters = this.userParameters
        )

private fun PricePerPeriod.mapToResponse() =
        PricePerPeriodResponse(
                totalPrice = this.totalPrice.toPriceResponse(),
                totalDiscountPrice = this.totalDiscountPrice.toPriceResponse(),
                totalPriceWithDiscount = this.totalPriceWithDiscount.toPriceResponse(),
                startAt = this.startAt.value,
                endAt = this.endAt.value,
                items = this.items.map {
                    PricePerPeriodResponse.Item(
                            compositionId = it.compositionId.value,
                            itemId = it.itemId.value,
                            price = it.price.toPriceResponse(),
                            discountPrice = it.discountPrice.toPriceResponse(),
                            priceWithDiscount = it.priceWithDiscount.toPriceResponse()
                    )
                }
        )

private fun br.com.zup.realwave.sales.manager.domain.Price.toPriceResponse() =
        br.com.zup.realwave.sales.manager.api.response.Price(
                amount = this.amount,
                scale = this.scale,
                currency = this.currency
        )

private fun HashMap<ProductTypeId, InstallationAttribute>.mapToResponse() =
        this.map { o ->
            InstallationAttributesResponse(
                    productTypeId = o.key.value, attributes = o.value
                    .attributes
            )
        }

private fun Reason.mapToResponse() =
        ReasonResponse(
                code = this.code,
                description = this.description
        )

@JvmName("PaymentListToPaymentResponseList")
private fun List<Payment.PaymentMethod>?.mapToResponse(): List<PaymentMethodResponse>? =
        this?.map {
            PaymentMethodResponse(
                    method = it.method,
                    methodId = it.methodId,
                    price = Price(
                            amount = it.price!!.amount,
                            scale = it.price!!.scale,
                            currency = it.price!!.currency
                    ),
                    customFields = it.customFields,
                    securityCodeInformed = it.securityCodeInformed
            )
        }

private fun PurchaseOrder.buildDiscount(): DiscountResponse? {

    return if (this.items.isNotEmpty() && this.coupon?.reward != null) {

        val discountPriceAmount = this.discountPrice().amount
        val fullPriceAmount = this.items.sumBy { it.firstPeriodPrice() }

        DiscountResponse(
                discountType = this.coupon?.reward?.type,
                fullPrice = Price(
                        amount = fullPriceAmount,
                        scale = this.items.first().price.scale,
                        currency = this.items.first().price.currency
                ),
                discountValue = Price(
                        amount = if (discountPriceAmount > fullPriceAmount) fullPriceAmount else discountPriceAmount,
                        scale = this.discountPrice().scale,
                        currency = this.discountPrice().currency
                ),
                finalPrice = toApiPrice(this.totalPrice())
        )
    } else null
}

private fun toApiPrice(price: br.com.zup.realwave.sales.manager.domain.Price): br.com.zup.realwave.sales.manager.api.response.Price = br.com.zup.realwave.sales.manager.api.response.Price(
        amount = price.amount,
        scale = price.scale,
        currency = price.currency
)