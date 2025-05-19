package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.validator.constraints.NotBlank

/**
 * Created by Danilo Paiva on 02/06/17
 */
data class Payment(
        var methods: List<PaymentMethod> = emptyList(),
        val description: Description? = null,
        val async: Boolean? = false) {
    companion object {
        fun couponPayment(): Payment = Payment(listOf(PaymentMethod(method = "REWARD", methodId = "REWARD")))
    }

    data class PaymentMethod(
        @field:NotBlank val method: String,
        val methodId: String?,
        val installments: Int? = null,
        val price: Price? = null,
        val customFields: JsonNode? = null,
        val securityCodeInformed: Boolean = false

    )

    data class Description(val value: String)

    init {
        Validator.validate(this)
    }

}
