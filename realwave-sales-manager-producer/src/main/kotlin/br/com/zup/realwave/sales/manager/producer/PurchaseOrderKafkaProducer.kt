package br.com.zup.realwave.sales.manager.producer

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus.OPENED
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus.CHECKED_OUT
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.producer.utils.eventBuilder
import br.com.zup.realwave.sales.manager.producer.utils.toStateChange
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
class PurchaseOrderKafkaProducer(
    @Value("\${kafka.topic.rw.sales.manager.events}")
    private val topic: String,
    private val kafkaTemplate: KafkaTemplate<String, String>
) : PurchaseOrderProducer {

    private companion object {
        const val PURCHASE_ORDER_CREATED = "PurchaseOrderCreated"
        const val PURCHASE_ORDER_CHECKED_OUT = "PurchaseOrderCheckedout"
        const val PURCHASE_ORDER_FINISHED = "PurchaseOrderFinished"
        const val DOMAIN = "SALES-MANAGER"
    }

    override fun notifyPurchaseOrderStateUpdated(purchaseOrder: PurchaseOrder) {

        val eventType = when(purchaseOrder.status) {
            OPENED -> PURCHASE_ORDER_CREATED
            CHECKED_OUT -> PURCHASE_ORDER_CHECKED_OUT
            else -> PURCHASE_ORDER_FINISHED
        }

        notify(purchaseOrder.idAsString(), eventType, purchaseOrder.toStateChange())

    }

    private fun <T> notify(eventId: String, eventType: String, event: T) {
        val timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()
        val kafkaEvent = eventBuilder(eventId, eventType, DOMAIN, timestamp, event)

        kafkaTemplate.send(topic, eventId, kafkaEvent)
    }

}
