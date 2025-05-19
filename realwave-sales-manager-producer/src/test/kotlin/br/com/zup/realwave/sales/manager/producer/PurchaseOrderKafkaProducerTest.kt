package br.com.zup.realwave.sales.manager.producer

import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderProducer
import br.com.zup.realwave.sales.manager.producer.helper.installationAttribute
import br.com.zup.realwave.sales.manager.producer.helper.planOfferItems
import br.com.zup.realwave.sales.manager.producer.helper.purchaseOrderItem
import br.com.zup.realwave.sales.manager.producer.helper.samplePurchaseOrder
import org.junit.Test
import org.springframework.kafka.core.KafkaTemplate
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify

class PurchaseOrderKafkaProducerTest {

    private val kafkaTemplate: KafkaTemplate<String, String> = mock { }

    private val purchaseOrderProducer: PurchaseOrderProducer =
        PurchaseOrderKafkaProducer(
            topic = "rw_sm_purchase_events",
            kafkaTemplate = kafkaTemplate
        )

    @Test
    fun `Notify that the state for a purchase order was updated`() {

        val purchaseOrder = samplePurchaseOrder(
            purchaseOrderId = PurchaseOrderId("52b444c6-6946-42e0-a55b-f7230a8f335b"),
            items = listOf(
                purchaseOrderItem(
                    catalogOfferId = CatalogOfferId("504999fd-f0c4-4eb0-967c-35493edbe493"),
                    type = "PLAN",
                    offerItems = planOfferItems()
                )
            ),
            installationAttribute = installationAttribute(
                "2f872db3-8157-40f9-90e4-a3b9c390605b",
                mapOf("iccid" to "1234")
            )
        )

        purchaseOrderProducer.notifyPurchaseOrderStateUpdated(purchaseOrder)

        verify(kafkaTemplate).send(eq("rw_sm_purchase_events"), eq("52b444c6-6946-42e0-a55b-f7230a8f335b"), any())
    }

}
