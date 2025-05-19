package br.com.zup.realwave.sales.manager.domain

import br.com.zup.test.realwave.sales.manager.domain.createPurchaseItemCoupon
import br.com.zup.test.realwave.sales.manager.domain.rescueCoupon
import org.junit.Test
import kotlin.test.assertEquals

class RescueServiceCouponTest {

    @Test
    fun createItem() {

        val coupon = rescueCoupon()
        val item = createPurchaseItemCoupon(coupon)

        assertEquals("COUPON", item.catalogOfferType.value)

        assertEquals(coupon.id, item.catalogOfferId.value)
        assertEquals(0, item.price.amount)
        assertEquals(coupon.validity, item.validity)
        assertEquals(1, item.offerItems.size)
        assertEquals(coupon.compositionId, item.offerItems[0].catalogOfferItemId.value)
    }

}
