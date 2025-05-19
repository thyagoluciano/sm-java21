package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank

data class SalesForceRequest(
    @field:NotBlank val id: String,
    @field:NotBlank val name: String
)
