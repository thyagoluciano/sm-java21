package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator

data class Channel(val value: String?) {

    init {
        Validator.validate(this)
    }

}
