package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank
import javax.validation.Valid
import javax.validation.constraints.Size

const val MAX_CUSTOMER_SIZE = 255

data class CustomerRequest(@field:[Valid NotBlank Size(max = MAX_CUSTOMER_SIZE)] val customer: String?)
