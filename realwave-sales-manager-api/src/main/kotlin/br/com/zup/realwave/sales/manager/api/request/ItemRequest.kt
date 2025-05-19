package br.com.zup.realwave.sales.manager.api.request

import br.com.zup.realwave.sales.manager.api.request.validation.ItemRequestValidation
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.validator.constraints.NotBlank
import javax.validation.Valid
import javax.validation.constraints.NotNull

@ItemRequestValidation
data class ItemRequest(
    @field:[NotNull Valid] val catalogOfferId: String?,
    @field:[NotNull Valid] val catalogOfferType: String?,
    @field:[NotNull Valid] val price: PriceRequest?,
    @field:[NotNull Valid] val validity: OfferValidity?,
    val offerFields: JsonNode? = ObjectMapper().createObjectNode(),
    val customFields: JsonNode? = ObjectMapper().createObjectNode(),
    @field:[NotNull Valid] val offerItems: List<OfferItem>?,
    @field:[NotNull Valid] val pricesPerPeriod: List<PricePerPeriodRequest>? = listOf(),
    val quantity: Int = 1
) {

    data class OfferItem(
        val productId: String?,
        @field:NotBlank val catalogOfferItemId: String?,
        @field:[NotNull Valid] val price: PriceRequest?,
        val recurrent: Boolean?,
        val customFields: JsonNode? = ObjectMapper().createObjectNode(),
        val userParameters: Map<String, Any>? = null
    )

    data class PricePerPeriodRequest(
        @field:[NotNull Valid] val totalPrice: PriceRequest?,
        @field:[NotNull Valid] val totalDiscountPrice: PriceRequest?,
        @field:[NotNull Valid] val totalPriceWithDiscount: PriceRequest?,
        @field:[NotNull Valid] val startAt: Int?,
        @field:[NotNull Valid] val endAt: Int?,
        @field:[NotNull Valid] val items: List<Item>?
    ) {

        data class Item(
            @field:[NotNull Valid] val compositionId: String?,
            @field:[NotNull Valid] val itemId: String?,
            @field:[NotNull Valid] val price: PriceRequest?,
            @field:[NotNull Valid] val discountPrice: PriceRequest?,
            @field:[NotNull Valid] val priceWithDiscount: PriceRequest?
        )
    }
}

data class OfferValidity(
    val period: String?,
    val duration: Int?,
    val unlimited: Boolean
)

data class PriceRequest(
    @field:NotBlank val currency: String?,
    @field:NotNull val amount: Int?,
    @field:NotNull val scale: Int?
)
