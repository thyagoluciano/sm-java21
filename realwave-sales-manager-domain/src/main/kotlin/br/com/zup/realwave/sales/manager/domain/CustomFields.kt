package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created by cleber on 6/5/17
 */
data class CustomFields(val value: JsonNode? = ObjectMapper().createObjectNode()) {
    fun objectToJson(): String {
        return value.objectToJson()
    }
}
