package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import com.fasterxml.jackson.databind.JsonNode
import javax.validation.constraints.NotNull

/**
 * Created by Danilo Paiva on 29/05/17
 */
data class Segmentation(@field:NotNull val query: JsonNode) {

    init {
        Validator.validate(this)
    }
}
