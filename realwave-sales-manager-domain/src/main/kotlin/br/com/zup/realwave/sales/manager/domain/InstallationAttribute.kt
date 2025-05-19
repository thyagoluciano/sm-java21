package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Created by marcosgm on 05/06/17.
 */
data class InstallationAttribute(
    @field:[NotNull Valid] val productTypeId: ProductTypeId,
    @field:NotNull val attributes: Map<String, Any>
) {

    init {
        Validator.validate(this)
    }

    fun attributesToJson(): String {
        return attributes.objectToJson()
    }
}
