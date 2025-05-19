package br.com.zup.realwave.sales.manager.api.request

import br.com.zup.realwave.sales.manager.api.request.validation.PaymentMethodValidation
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.validator.constraints.NotBlank
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class PaymentRequest(
    @field:[Valid] val methods: List<PaymentMethodRequest>,
    val description: String?,
    val async: Boolean? = false
) {
    @PaymentMethodValidation
    data class PaymentMethodRequest(
        @field:NotBlank val method: String?,
        val methodId: String?,
        val price: Price? = null,
        val customFields: JsonNode?,
        val installments: Int? = null
    )

    data class Price(
        @field:NotBlank val currency: String,
        @field:NotNull val amount: Int,
        @field:NotNull val scale: Int
    )
}
