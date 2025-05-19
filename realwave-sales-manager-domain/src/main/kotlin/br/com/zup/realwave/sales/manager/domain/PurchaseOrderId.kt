package br.com.zup.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.infrastructure.Validator
import java.util.UUID

/**
 * Created by luizs on 24/05/2017
 */
class PurchaseOrderId(value: String = UUID.randomUUID().toString()) : AggregateId(value) {
    init {
        Validator.validate(this)
    }

    override fun toString(): String {
        return value
    }
}
