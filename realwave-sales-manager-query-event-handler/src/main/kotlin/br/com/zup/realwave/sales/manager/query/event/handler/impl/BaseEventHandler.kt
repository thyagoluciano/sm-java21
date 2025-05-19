package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.Event
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.domain.repository.CustomerOrderRepository
import br.com.zup.realwave.sales.manager.domain.repository.FreightRepository
import br.com.zup.realwave.sales.manager.domain.repository.PaymentRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseEventHandler<in T : Event> {

    protected val log = LogManager.getLogger(this.javaClass)

    @Autowired
    lateinit var purchaseOrderRepository: PurchaseOrderRepository

    @Autowired
    lateinit var customerOrderRepository: CustomerOrderRepository

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Autowired
    lateinit var freightRepository: FreightRepository

    abstract fun handle(event: T, metaData: MetaData, version: AggregateVersion)

}
