package br.com.zup.realwave.sales.manager.api.request.validation

import br.com.zup.realwave.sales.manager.api.request.ItemRequest
import br.com.zup.realwave.sales.manager.api.request.OfferValidity
import br.com.zup.realwave.sales.manager.api.request.PriceRequest
import org.mockito.Mockito
import javax.validation.ConstraintValidatorContext

fun buildItemRequest(
    validity: OfferValidity = OfferValidity(
        period = "period",
        duration = 10,
        unlimited = false
    )
) = ItemRequest(
    catalogOfferId = "catalog-offer-id",
    catalogOfferType = "catalog-offer-type",
    price = PriceRequest(
        currency = "BRL",
        amount = 10,
        scale = 2
    ),
    validity = validity,
    offerItems = listOf(),
    pricesPerPeriod = listOf()
)

fun buildConstraintValidatorContextMock() =
    Mockito.mock(ConstraintValidatorContext::class.java)
        .apply {
            val constraintViolationBuilder =
                Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder::class.java)
            val nodeBuilderCustomizableContext =
                Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext::class.java)
            Mockito
                .`when`(this.buildConstraintViolationWithTemplate(Mockito.any()))
                .thenReturn(constraintViolationBuilder)
            Mockito
                .`when`(constraintViolationBuilder.addPropertyNode(Mockito.any()))
                .thenReturn(nodeBuilderCustomizableContext)
        }
