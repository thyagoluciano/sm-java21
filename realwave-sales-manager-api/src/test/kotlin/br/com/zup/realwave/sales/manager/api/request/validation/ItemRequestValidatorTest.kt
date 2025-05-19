package br.com.zup.realwave.sales.manager.api.request.validation

import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.OfferValidity
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItemRequestValidatorTest {

    val context = buildConstraintValidatorContextMock()

    val validator = ItemRequestValidator().apply {
        initialize(ItemRequest::class.java.getAnnotation(ItemRequestValidation::class.java))
    }

    @Test
    fun `test validation for valid item`() {
        val validItem = buildItemRequest()
        assertTrue(validator.isValid(validItem, context))
    }

    @Test
    fun `test validation failure for invalid item`() {
        val invalidItem1 = buildItemRequest(OfferValidity(period = null, duration = 10, unlimited = false))
        val invalidItem2 = buildItemRequest(OfferValidity(period = "DAYS", duration = null, unlimited = false))
        assertFalse(validator.isValid(invalidItem1, context))
        assertFalse(validator.isValid(invalidItem2, context))
    }

}
