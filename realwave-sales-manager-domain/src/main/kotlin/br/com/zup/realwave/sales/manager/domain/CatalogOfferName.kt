package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator

/**
 * Created by cleber on 6/5/17.
 */
data class CatalogOfferName(val value: String?) {
    init {
        Validator.validate(this)
    }
}
