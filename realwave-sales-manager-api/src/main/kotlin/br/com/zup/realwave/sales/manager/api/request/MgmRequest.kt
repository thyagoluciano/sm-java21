package br.com.zup.realwave.sales.manager.api.request

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.validator.constraints.NotBlank

data class MgmRequest(
    @field:NotBlank val code: String?,
    val customFields: JsonNode? = ObjectMapper().createObjectNode()
)
