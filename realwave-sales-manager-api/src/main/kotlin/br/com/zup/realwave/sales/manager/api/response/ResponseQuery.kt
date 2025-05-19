package br.com.zup.realwave.sales.manager.api.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.validator.constraints.NotBlank
import javax.validation.constraints.NotNull

data class PurchaseOrderResponse(
    val id: String,
    val type: String?,
    val protocol: String?,
    var subscriptionId: String?,
    val segmentation: JsonNode?,
    val mgm: MgmResponse?,
    val salesForce: SalesForceResponse?,
    val onBoardingSale: OnBoardingSaleResponse?,
    val customer: CustomerResponse?,
    val coupon: CouponResponse?,
    val totalPrice: Price?,
    val discount: DiscountResponse?,
    val payment: PaymentResponse,
    val freight: FreightResponse? = null,
    val status: String?,
    val items: List<ItemResponse>?,
    val installationAttributes: List<InstallationAttributesResponse>?,
    val channelCreate: Channel?,
    val channelCheckout: Channel?,
    val callback: CallbackResponse?,
    val reason: ReasonResponse?,
    val createdAt: String?,
    val updatedAt: String?
)

class Channel(val value: String?)

data class CallbackResponse(val url: String, val headers: JsonNode?)

data class CustomerOrderResponse(
    val customerOrderId: String?,
    val status: String?,
    val steps: List<StepResponse>?
)

data class StepResponse(
    val step: String?,
    val status: String?,
    val startedAt: String?,
    val endedAt: String?
)

data class ItemResponse(
    val id: String?,
    val catalogOfferId: String?,
    val catalogOfferType: String?,
    val price: Price,
    val validity: OfferValidity,
    val customFields: JsonNode?,
    val offerItems: List<OfferItemResponse>,
    val pricesPerPeriod: List<PricePerPeriodResponse>,
    val quantity: Int? = 1
)

data class OfferItemResponse(
    val productId: String?,
    val catalogOfferItemId: String,
    val price: Price,
    val customFields: JsonNode?,
    val recurrent: Boolean?,
    val userParameters: Map<String, Any>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Price(
    @field:NotBlank val currency: String,
    @field:NotNull val amount: Int,
    @field:NotNull val scale: Int
)

data class OfferValidity(
    val period: String?,
    val duration: Int?,
    val unlimited: Boolean
)

data class DiscountResponse(
    val fullPrice: Price?,
    val discountValue: Price?,
    val discountType: String?,
    val finalPrice: Price?
)

data class Description(val value: String?)

data class CustomerResponse(val id: String?)

data class MgmResponse(val code: String?, val fields: JsonNode?)

data class SalesForceResponse(val id: String?, val name: String?)

data class CouponResponse(val id: String?, val fields: JsonNode?)

data class OnBoardingSaleResponse(val id: String?, val fields: JsonNode?)

data class PaymentResponse(val methods: List<PaymentMethodResponse>?, val description: Description?)

data class PaymentMethodResponse(
    val method: String? = null,
    val methodId: String? = null,
    val price: Price? = null,
    val customFields: JsonNode?,
    val securityCodeInformed: Boolean,
    val installments: Int? = null
)

data class FreightResponse(
    val address: Address,
    val price: Price,
    val type: String,
    val deliveryTotalTime: Int
) {
    data class Address(
        val city: String,
        val complement: String,
        val country: String,
        val district: String,
        val name: String,
        val state: String,
        val street: String,
        val number: String,
        val zipCode: String
    )

    data class Price(
        val currency: String,
        val amount: Int,
        val scale: Int
    )
}

data class InstallationAttributesResponse(val productTypeId: String, val attributes: Map<String, Any>)

data class PurchaseOrderStatusResponse(val status: String?, val customerOrder: CustomerOrderResponse?)

data class ReasonResponse(val code: String?, val description: String?)

data class PricePerPeriodResponse(
    val totalPrice: Price,
    val totalDiscountPrice: Price,
    val totalPriceWithDiscount: Price,
    val startAt: Int,
    val endAt: Int,
    val items: List<Item>
) {

    data class Item(
        val compositionId: String,
        val itemId: String,
        val price: Price,
        val discountPrice: Price,
        val priceWithDiscount: Price
    )
}
