package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Created by cleber on 6/5/17.
 */
data class Item(
    @field:[NotNull Valid] val id: Id = Id(),
    @field:[NotNull Valid] val catalogOfferId: CatalogOfferId,
    @field:[NotNull Valid] val catalogOfferType: CatalogOfferType,
    @field:[NotNull Valid] val price: Price,
    @field:[NotNull Valid] val validity: OfferValidity,
    val offerFields: OfferFields,
    val customFields: CustomFields,
    @field:[NotNull NotEmpty Valid] val offerItems: List<OfferItem>,
    @field:[NotNull Valid] val pricesPerPeriod: List<PricePerPeriod> = listOf(),
    val quantity: Quantity = Quantity()
) {

    companion object {

        fun equals(a: Item, b: Item): Boolean =
            a === b || a.id == b.id
    }

    init {
        Validator.validate(this)
    }

    data class Quantity(val value: Int = 1)

    data class Id(val value: String = UUID.randomUUID().toString()) {
        override fun toString(): String = value
    }

    data class OfferItem(
        val productId: ProductId? = null,
        @field:[NotNull Valid] val catalogOfferItemId: CatalogOfferItemId,
        @field:[NotNull Valid] val price: Price,
        val recurrent: Boolean = false,
        val customFields: CustomFields? = null,
        val userParameters: Map<String, Any>? = null
    ) {
        init {
            Validator.validate(this)
        }

        data class CatalogOfferItemId(@field:NotBlank val value: String) {
            init {
                Validator.validate(this)
            }
        }

        data class CatalogOfferItemName(val value: String?) {
            init {
                Validator.validate(this)
            }
        }

    }

    fun customFieldsToJson() = customFields.objectToJson()
    fun offerFieldsToJson() = offerFields.objectToJson()
    fun offerItemsToJson() = offerItems.objectToJson()
    fun pricesPerPeriodToJson() = pricesPerPeriod.objectToJson()

    fun firstPeriodPrice(): Int {

        if (pricesPerPeriod.isNotEmpty()) {
            return pricesPerPeriod.first {
                it.startAt.value == 1
            }.totalPriceWithDiscount.amount
        }

        return price.amount
    }

    override fun equals(other: Any?): Boolean =
        other != null
                && other.javaClass == javaClass
                && equals(this, other as Item)

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
