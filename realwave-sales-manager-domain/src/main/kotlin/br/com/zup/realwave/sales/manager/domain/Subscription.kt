package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator

class Subscription(val id: String) {

    init {
        Validator.validate(this)
    }

    override fun toString(): String {
        return id
    }
}
