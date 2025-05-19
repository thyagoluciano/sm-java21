package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import org.hibernate.validator.constraints.NotBlank

/**
 * Created by cleber on 6/1/17.
 */
data class CatalogOfferId(@field:NotBlank val value: String) {

    init {
        Validator.validate(this)
    }

}
