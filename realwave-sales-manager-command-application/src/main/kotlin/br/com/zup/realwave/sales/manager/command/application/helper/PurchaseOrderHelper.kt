package br.com.zup.realwave.sales.manager.command.application.helper

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.api.request.CallbackRequest
import br.com.zup.realwave.sales.manager.api.request.FreightRequest
import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.PaymentRequest
import br.com.zup.realwave.sales.manager.api.request.PriceRequest
import br.com.zup.realwave.sales.manager.api.response.BoletoResponse
import br.com.zup.realwave.sales.manager.domain.Boleto
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.api.request.Reason as ReasonApi

object PurchaseOrderHelper {

    fun validatePaymentRequestList(paymentRequestList: PaymentRequest) {
        if (paymentRequestList.methods.size > 1) {
            if (paymentRequestList.methods.any { it.price == null }) {
                throw BusinessException.of("price", PurchaseOrderErrorCode.PRICE_MUST_BE_INFORMED)
            }
        }
    }

    fun fromPrice(price: PaymentRequest.Price): Price =
        Price(
            amount = price.amount,
            scale = price.scale,
            currency = price.currency
        )

    fun fromReason(reason: ReasonApi): Reason =
        Reason(
            code = reason.code,
            description = reason.description
        )

    fun callback(callbackRequest: CallbackRequest?) =
        if (callbackRequest == null) null
        else fromCallback(callbackRequest)

    fun fromCallback(callbackRequest: CallbackRequest): Callback =
        Callback(callbackRequest.url, callbackRequest.headers)

    fun fromCustomerOrder(id: String, status: String, steps: List<Step>) =
        CustomerOrder(customerOrderId = id, status = status, steps = steps)

    fun fromItem(itemRequest: ItemRequest, itemId: Item.Id = Item.Id()) =
        Item(
            id = itemId,
            catalogOfferId = CatalogOfferId(itemRequest.catalogOfferId!!),
            catalogOfferType = CatalogOfferType(itemRequest.catalogOfferType!!),
            price = Price(
                currency = itemRequest.price!!.currency!!,
                amount = itemRequest.price!!.amount!!,
                scale = itemRequest.price!!.scale!!
            ),
            validity = OfferValidity(
                period = itemRequest.validity!!.period,
                duration = itemRequest.validity!!.duration,
                unlimited = itemRequest.validity!!.unlimited
            ),
            offerFields = OfferFields(itemRequest.offerFields),
            customFields = CustomFields(itemRequest.customFields),
            offerItems = itemRequest.offerItems!!.map { offerItem -> fromOfferItem(offerItem) },
            pricesPerPeriod = itemRequest.pricesPerPeriod!!.map { fromPricePerPeriod(it) },
            quantity = Item.Quantity(value = itemRequest.quantity)
        )

    private fun fromOfferItem(offerItem: ItemRequest.OfferItem): Item.OfferItem {
        var productId: ProductId? = null
        if (offerItem.productId != null) {
            productId = ProductId(offerItem.productId!!)
        }
        return Item.OfferItem(
            productId = productId,
            price = Price(
                currency = offerItem.price!!.currency!!,
                scale = offerItem.price!!.scale!!,
                amount = offerItem.price!!.amount!!
            ),
            catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(offerItem.catalogOfferItemId!!),
            recurrent = offerItem.recurrent ?: false,
            customFields = CustomFields(offerItem.customFields),
            userParameters = offerItem.userParameters
        )
    }

    fun toBoletoResponse(boleto: Boleto?): BoletoResponse? {
        return if (boleto != null) {
            BoletoResponse(
                methodId = boleto.methodId,
                payload = boleto.payload
            )
        } else {
            null
        }
    }

    private fun fromPricePerPeriod(price: ItemRequest.PricePerPeriodRequest) =
        PricePerPeriod(
            totalPrice = fromPrice(price.totalPrice!!),
            totalDiscountPrice = fromPrice(price.totalDiscountPrice!!),
            totalPriceWithDiscount = fromPrice(price.totalPriceWithDiscount!!),
            startAt = PricePerPeriod.StartAt(price.startAt!!),
            endAt = PricePerPeriod.EndAt(price.endAt!!),
            items = price.items!!.map { fromItem(it) }
        )

    private fun fromPrice(price: PriceRequest): Price =
        Price(
            amount = price.amount!!,
            scale = price.scale!!,
            currency = price.currency!!
        )

    private fun fromItem(pricePerPeriod: ItemRequest.PricePerPeriodRequest.Item) =
        PricePerPeriod.Item(
            compositionId = PricePerPeriod.Item.CompositionId(pricePerPeriod.compositionId!!),
            itemId = PricePerPeriod.Item.ItemId(pricePerPeriod.itemId!!),
            price = fromPrice(pricePerPeriod.price!!),
            discountPrice = fromPrice(pricePerPeriod.discountPrice!!),
            priceWithDiscount = fromPrice(pricePerPeriod.priceWithDiscount!!)
        )

}
