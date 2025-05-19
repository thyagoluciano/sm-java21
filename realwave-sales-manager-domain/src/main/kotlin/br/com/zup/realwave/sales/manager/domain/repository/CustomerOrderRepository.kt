package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.CustomerOrder

/**
 * Created by branquinho on 11/07/17.
 */
interface CustomerOrderRepository {

    fun findOne(purchaseOrderId: AggregateId): CustomerOrder?

    fun saveCustomerOrder(purchaseOrderId: AggregateId, customerOrder: CustomerOrder): Int

}
