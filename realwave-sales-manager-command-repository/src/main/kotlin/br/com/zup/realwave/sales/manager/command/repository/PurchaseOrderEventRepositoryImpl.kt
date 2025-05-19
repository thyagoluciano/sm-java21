package br.com.zup.realwave.sales.manager.command.repository

import br.com.zup.eventsourcing.core.AggregateRoot
import br.com.zup.eventsourcing.core.MetaData
import br.com.zup.eventsourcing.eventstore.EventStoreRepository
import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.stereotype.Service
import javax.annotation.Priority

@Service
@Priority(1)
open class PurchaseOrderEventRepositoryImpl(val counterService: CounterService) :
    EventStoreRepository<PurchaseOrder>() {

    override fun save(aggregateRoot: AggregateRoot, lock: OptimisticLock): PurchaseOrder {
        val eventcounts = aggregateRoot.events.size
        val purchase = super.save(aggregateRoot, loadMetaData(), lock)
        for (i in 0 until eventcounts)
            counterService.increment("count.event.saved")
        return purchase
    }

    private fun loadMetaData(): MetaData {
        var metaData = MetaData()
        metaData.put(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER, RealwaveContextHolder.getContext().organization)
        metaData.put(RealwaveContextConstants.APPLICATION_ID_HEADER, RealwaveContextHolder.getContext().application)
        metaData.put(RealwaveContextConstants.TRACKING_ID_HEADER, RealwaveContextHolder.getContext().globalTrackingId)
        metaData.put(
            RealwaveContextConstants.TRACKING_CONTEXT_HEADER,
            RealwaveContextHolder.getContext().globalTrackingId
        )
        metaData.put(RealwaveContextConstants.CHANNEL_CONTEXT_HEADER, RealwaveContextHolder.getContext().channel)
        return metaData

    }

}
