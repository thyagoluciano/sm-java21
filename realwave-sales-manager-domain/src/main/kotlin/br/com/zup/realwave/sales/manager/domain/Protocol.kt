package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator

class Protocol(val value: String) {

    init {
        Validator.validate(this)
    }

    override fun toString(): String {
        return value
    }
}
