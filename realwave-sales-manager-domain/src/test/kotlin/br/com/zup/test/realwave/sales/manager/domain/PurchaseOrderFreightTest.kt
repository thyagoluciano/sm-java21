package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderFreightUpdated
import br.com.zup.realwave.sales.manager.domain.updateFreight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseOrderFreightTest {

    @Test
    fun testUpdateFreight() {
        val purchaseOrder = buildPurchaseOrder()
        val freight = Freight(
            type = Freight.Type("BR"),
            price = Price(
                currency = "BRL",
                amount = 2990,
                scale = 2
            ),
            address = Freight.Address(
                city = "Uberlândia",
                complement = "7o. Andar",
                country = "Brazil",
                district = "Tibery",
                name = "ZUP",
                state = "MG",
                street = "Av Rondon Pacheco",
                zipCode = "38400000",
                number = "2345"
            ),
            deliveryTotalTime = Freight.DeliveryTotalTime(3)
        )

        purchaseOrder.updateFreight(freight)
        assertEquals(expected = freight, actual = purchaseOrder.freight)
        assertTrue(purchaseOrder.event is PurchaseOrderFreightUpdated)
    }
}
