package br.com.zup.realwave.sales.manager.api.request.validation

import br.com.zup.realwave.sales.manager.api.request.PurchaseOrderRequest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurchaseOrderTypeValidationTest {

    val context = buildConstraintValidatorContextMock()

    val validator = PurchaseOrderTypeValidator().apply {
        initialize(PurchaseOrderRequest::class.java.getAnnotation(PurchaseOrderTypeValidation::class.java))
    }

    @Test
    fun `test validation for valid purchase order type`() {
        PurchaseOrderTypeValidator.PurchaseOrderType.values()
            .map {
                val type = it.name
                val validPurchaseOrderRequest = PurchaseOrderRequest(type = type)
                assertTrue(validator.isValid(validPurchaseOrderRequest, context))
            }
    }

    @Test
    fun `test validation failure for invalid purchase order type`() {
        val invalidPurchaseOrderRequest = PurchaseOrderRequest(type = "INVALID_TYPE")
        assertFalse(validator.isValid(invalidPurchaseOrderRequest, context))
    }

}
