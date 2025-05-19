package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty

data class InstallationAttributesRequest(
    @field:NotBlank val productTypeId: String?,
    @field:NotEmpty val attributes: Map<String, Any>?
)
