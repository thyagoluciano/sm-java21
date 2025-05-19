package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Freight

interface FreightRepository {

    fun find(purchaseOrderId: AggregateId): Freight?

    fun saveFreight(purchaseOrderId: AggregateId, freight: Freight): Int

}
