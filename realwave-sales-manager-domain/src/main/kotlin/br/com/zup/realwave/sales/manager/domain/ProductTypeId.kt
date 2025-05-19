package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import org.hibernate.validator.constraints.NotBlank

/**
 * Created by luizs on 26/07/2017
 */
data class ProductTypeId(@field:NotBlank val value: String) {
    init {
        Validator.validate(this)
    }

    override fun toString(): String {
        return value
    }
}
