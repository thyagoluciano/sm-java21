package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class FreightRequest(
    @field:Valid val address: Address,
    @field:Valid val price: Price,
    @field:[Valid NotBlank] val type: String?,
    @field:[Valid NotNull] val deliveryTotalTime: Int?
) {
    data class Address(
        @field:[Valid NotBlank] val city: String?,
        val complement: String? = null,
        @field:[Valid NotBlank] val country: String?,
        @field:[Valid NotBlank] val district: String?,
        @field:[Valid NotBlank] val name: String?,
        @field:[Valid NotBlank] val state: String?,
        @field:[Valid NotBlank] val street: String?,
        @field:[Valid NotBlank] val number: String?,
        @field:[Valid NotBlank] val zipCode: String?
    )

    data class Price(
        @field:[Valid NotBlank] val currency: String?,
        @field:[Valid NotNull] val amount: Int?,
        @field:[Valid NotNull] val scale: Int?
    )
}
