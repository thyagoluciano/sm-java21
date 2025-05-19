package br.com.zup.realwave.sales.manager.query.event.handler.subscriber

import akka.actor.ActorSystem
import akka.util.Timeout
import br.com.zup.eventsourcing.core.EventHandler
import br.com.zup.eventsourcing.eventstore.PersistentAggregateSubscriber
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.integration.feign.apis.EventStoreApiService
import eventstore.akka.ProjectionsClient
import eventstore.akka.Settings
import eventstore.core.InvalidOperationException
import eventstore.j.EsConnection
import eventstore.j.EsConnectionFactory
import eventstore.j.PersistentSubscriptionSettingsBuilder
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by marcosgm on 26/06/17.
 */
@Service
class PurchaseOrderAggregateSubscriber(private val eventStoreApiService: EventStoreApiService, eventHandler: EventHandler) :
    PersistentAggregateSubscriber<PurchaseOrder>(
        subscriptionGroupName = "CategorySubscriptionGroup",
        eventHandler = eventHandler
    ) {

    companion object {
        const val INDEX = "fromCategory('PurchaseOrder').foreachStream().when({\$any : function" +
                "(s,e) { linkTo(\"PurchaseOrder\", e); }})"
        const val TIMEOUT_CREATE_PERSISTENT_GROUP = 60L
    }

    override fun getSubscriptionName(): String {
        return "\$ce-PurchaseOrder"
    }

    private val log = LogManager.getLogger(this.javaClass)

    private val esConnection: EsConnection = EsConnectionFactory.create(actorSystem)

    @Value("\${event.sourcing.subscribe}")
    var eventSourcingSubscribes: String? = null

    private val timeout = Timeout(Duration.create(TIMEOUT_CREATE_PERSISTENT_GROUP, "seconds"))

    override fun start() {

        enableSystemProjections()

//        createIndex(actorSystem)

        createPersistentGroup()

        var qtd = 1
        if (eventSourcingSubscribes != null) {
            qtd = Integer.parseInt(eventSourcingSubscribes)
        }

        for (i in 0 until qtd) {
            super.start()
        }
    }

    private fun enableSystemProjections() {
        eventStoreApiService.enableProjection("\$by_category")
    }

    private fun createPersistentGroup() {
        val stream = "\$ce-PurchaseOrder"
        val groupName = "CategorySubscriptionGroup"
        log.info(
            "Creating the persistent group {}, it waits {} seconds until the creation.",
            stream, timeout.duration().length(), timeout.duration().unit()
        )

        val future = esConnection.createPersistentSubscription(
            stream,
            groupName,
            PersistentSubscriptionSettingsBuilder()
                .resolveLinkTos()
                .roundRobin()
                .withExtraStatistic()
                .startFromCurrent()
                .build(),
            null
        )

        try {
            Await.result(future, timeout.duration())
        } catch (ex: InvalidOperationException) {
            log.warn("SubscriptionGroup already exists {} [{}]", groupName, ex.message)
        }
    }

    private fun createIndex(actorSystem: ActorSystem) {
        val client = ProjectionsClient(Settings.Default(), actorSystem)
        client.createProjection(
            PurchaseOrder::class.java.simpleName, INDEX,
            ProjectionsClient.`ProjectionMode$`.`MODULE$`.apply("Continuous"), true
        )
    }

}
