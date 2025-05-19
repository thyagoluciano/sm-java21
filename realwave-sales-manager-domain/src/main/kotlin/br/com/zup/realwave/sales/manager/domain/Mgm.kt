package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.validator.constraints.NotBlank

/**
 * Created by marcosgm on 01/06/17.
 */
data class Mgm(@field:NotBlank val code: String, var customFields: JsonNode? = null) {

    init {
        Validator.validate(this)
    }

    fun customFieldsToStringJson() = customFields?.objectToJson()

}
