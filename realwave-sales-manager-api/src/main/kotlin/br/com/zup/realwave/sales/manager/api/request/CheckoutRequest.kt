package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class CheckoutRequest(@field:[NotNull Valid] val paymentSecurityCodes: List<SecurityCode>?) {

    data class SecurityCode(
        @field:NotBlank val methodId: String?,
        @field:NotBlank val securityCode: String?
    )

}
