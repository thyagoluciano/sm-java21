package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.validator.constraints.NotBlank
import java.math.BigDecimal
import javax.validation.constraints.NotNull

/**
 * Created by Danilo Paiva on 08/06/17
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Price(
    @field:NotBlank val currency: String,
    @field:NotNull val amount: Int,
    @field:NotNull val scale: Int
) {

    companion object {

        private const val DEFAULT_CURRENCY = "BRL"

        fun zero(): Price = Price(
            currency = DEFAULT_CURRENCY,
            amount = 0,
            scale = 0
        )

    }

    init {
        Validator.validate(this)
    }

    override fun toString(): String {
        return "${BigDecimal.valueOf(amount.toLong(), scale)} $currency"
    }
}
