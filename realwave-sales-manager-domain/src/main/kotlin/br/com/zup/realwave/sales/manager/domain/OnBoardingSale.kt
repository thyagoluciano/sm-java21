package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import com.fasterxml.jackson.databind.JsonNode
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Created by cleber on 6/1/17.
 */
data class OnBoardingSale(@field:[NotNull Valid] val offer: CatalogOfferId, var customFields: JsonNode?) {
    init {
        Validator.validate(this)
    }
}
