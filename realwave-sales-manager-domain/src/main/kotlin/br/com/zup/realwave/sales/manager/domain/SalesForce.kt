package br.com.zup.realwave.sales.manager.domain

import org.hibernate.validator.constraints.NotBlank

data class SalesForce(@field:NotBlank val id: String, @field:NotBlank val name: String)
