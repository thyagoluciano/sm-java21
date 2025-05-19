package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderTypeUpdated
import java.math.BigDecimal
import kotlin.math.min

fun equals(a: PurchaseOrder, b: PurchaseOrder): Boolean =
    a === b || a.id == b.id

fun PurchaseOrder.updatePurchaseOrderType(purchaseOrderType: PurchaseOrderType?) {
    verifyPurchaseOrderIsOpen()
    verifyPurchaseOrderType()
    applyChange(PurchaseOrderTypeUpdated(id, purchaseOrderType))
}

fun PurchaseOrder.deletePurchaseOrder() {
    canDelete()
    applyChange(PurchaseOrderDeleted(aggregateId = id))
}

fun PurchaseOrder.totalPrice(): Price {
    return if (this.items.isNotEmpty()) {
        val finalPriceAmount = this.items.sumBy { it.firstPeriodPrice() } - this.discountPrice().amount
        Price(
            amount = if (finalPriceAmount > 0) finalPriceAmount else 0,
            scale = this.items.first().price.scale,
            currency = this.items.first().price.currency
        )
    } else Price.zero()
}

//TODO IMPLEMENTS SEGMENT
fun PurchaseOrder.discountPrice(): Price =
    coupon?.reward?.type
        ?.takeIf { items.isNotEmpty() }
        ?.let { type ->
            when (type) {
                CouponCode.RewardType.DISCOUNT_MONEY.name ->
                    discountPriceMoney()
                CouponCode.RewardType.DISCOUNT_PERCENT.name ->
                    discountPricePercent()
                else ->
                    Price.zero()
            }
        } ?: Price.zero()

private fun PurchaseOrder.discountPriceMoney() =
    firstDiscount()?.discount
        ?.let { discount ->
            Price(
                currency = discount.currency,
                scale = discount.scale,
                amount = min(discount.amount, itemsTotalAmount())
            )
        } ?: Price.zero()

private fun PurchaseOrder.discountPricePercent() =
    firstItemPrice()
        .let { firstItemPrice ->
            Price(
                currency = firstItemPrice.currency,
                scale = firstItemPrice.scale,
                amount = min(firstDiscountPercent(), itemsTotalAmount())
            )
        }

private fun PurchaseOrder.firstItemPrice() =
    items.first().price

private fun PurchaseOrder.firstDiscount() =
    coupon?.reward?.discounts?.first()

private fun PurchaseOrder.itemsTotalAmount(): Int {
    return items.sumBy {
        it.firstPeriodPrice()
    }
}

private fun PurchaseOrder.itemsTotalAmountPrice() =
    BigDecimal.valueOf(itemsTotalAmount().toLong(), firstItemPrice().scale).toDouble()

private fun PurchaseOrder.firstDiscountPercent(): Int =
    firstDiscount()
        ?.let { discount ->
            (itemsTotalAmountPrice() * discount.discountAsPercent!!).toInt()
        } ?: 0
