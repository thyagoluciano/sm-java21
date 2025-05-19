package br.com.zup.realwave.sales.manager.query.event.handler.impl

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.Event
import br.com.zup.eventsourcing.core.EventHandler
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.realwave.sales.manager.infrastructure.common.liquibase.LiquibaseHandler
import br.com.zup.realwave.sales.manager.infrastructure.context.util.SalesManagerContextUtil
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.stereotype.Service

/**
 * Created by Danilo Paiva on 31/05/17
 */
@Service
class PurchaseOrderEventHandlerImpl @Autowired constructor(
    val liquibaseHandler: LiquibaseHandler,
    val counterService: CounterService,
    val eventHandlerDiscoveryService: EventHandlerDiscoveryService
) : EventHandler {

    private val log = LogManager.getLogger(this.javaClass)

    override fun handle(aggregateId: AggregateId, event: Event, metaData: MetaData, version: AggregateVersion) {
        try {
            log.debug("Receive event with aggregateId: [{}].", aggregateId.value)

            SalesManagerContextUtil.loadContext(metaData)

            liquibaseHandler.handleTenant()

            eventHandlerDiscoveryService.getEventHandler(event).handle(event, metaData, version)

            counterService.increment("count.events.handled")
        } catch (e: Exception) {
            log.error(
                "Error handling event of type ${event.retrieveEventType()}. aggregateId=${aggregateId.value}," +
                        " metaData=$metaData, version=${version.value}", e
            )
            // println("ERROR !!!!!!!!!!!!!!!! : ${aggregateId.value}")
            throw e
        }
        // println("Receive event with aggregateId: ${aggregateId.value}")
    }

}
