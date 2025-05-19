package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator

data class OfferValidity(
    val period: String?,
    val duration: Int?,
    val unlimited: Boolean
) {

    init {
        Validator.validate(this)
    }

}
