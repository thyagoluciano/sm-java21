package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.Validator
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class PricePerPeriod(
    @field:[NotNull Valid] val totalPrice: Price,
    @field:[NotNull Valid] val totalDiscountPrice: Price,
    @field:[NotNull Valid] val totalPriceWithDiscount: Price,
    @field:[NotNull Valid] val startAt: StartAt,
    @field:[NotNull Valid] val endAt: EndAt,
    @field:[NotNull Valid] val items: List<Item>
) {

    init {
        Validator.validate(this)
    }

    data class StartAt(@field:[NotNull Valid] val value: Int) {
        init {
            Validator.validate(this)
        }
    }

    data class EndAt(@field:[NotNull Valid] val value: Int) {
        init {
            Validator.validate(this)
        }
    }

    data class Item(
        @field:[NotNull Valid] val compositionId: CompositionId,
        @field:[NotNull Valid] val itemId: ItemId,
        @field:[NotNull Valid] val price: Price,
        @field:[NotNull Valid] val discountPrice: Price,
        @field:[NotNull Valid] val priceWithDiscount: Price
    ) {
        init {
            Validator.validate(this)
        }

        data class CompositionId(@field:[NotNull Valid] val value: String) {
            init {
                Validator.validate(this)
            }
        }

        data class ItemId(@field:[NotNull Valid] val value: String) {
            init {
                Validator.validate(this)
            }
        }
    }
}