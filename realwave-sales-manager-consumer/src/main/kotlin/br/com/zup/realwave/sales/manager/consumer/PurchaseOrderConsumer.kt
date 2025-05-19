package br.com.zup.realwave.sales.manager.consumer

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.service.CallbackService
import br.com.zup.realwave.sales.manager.events.PurchaseOrderChangeEvent
import br.com.zup.realwave.sales.manager.events.utils.extractEvent
import org.apache.logging.log4j.LogManager
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_MESSAGE_KEY
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION_ID
import org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
open class PurchaseOrderConsumer(
    val callbackService: CallbackService,
    val repositoryManager: Repository<PurchaseOrder>
) {

    private val logger = LogManager.getLogger("br.com.zup.realwave.sales.manager.consumer.PurchaseOrderConsumer")

    @KafkaListener(
        topics = ["\${kafka.topic.rw.sales.manager.events}"],
        groupId = "\${sm.kafka.purchase.order.status.group-id}"
    )
    fun receive(
        @Header(RECEIVED_TOPIC) topic: String,
        @Header(RECEIVED_PARTITION_ID) partition: Int,
        @Header(RECEIVED_MESSAGE_KEY) key: String,
        message: String
    ) {

        logger.info(
            "Listener for topic $topic, partition $partition, key $key, message $message",
            topic,
            partition,
            key,
            message
        )

        val eventKafka = message.extractEvent<PurchaseOrderChangeEvent>()

        val purchaseOrderId = eventKafka.payload.event.purchaseOrder.purchaseOrderId

        val purchaseOrder = repositoryManager.get(AggregateId(purchaseOrderId))

        callbackService.takeIf { purchaseOrder.callback != null }?.notify(purchaseOrder)

    }

}
