package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.Item.OfferItem.CatalogOfferItemId

data class RescueServiceCoupon(
    val code: String,
    val id: String,
    val compositionId: String,
    val validity: OfferValidity
) {

    fun createPurchaseOrderItem(productId: ProductId): Item =
        Item(
            catalogOfferId = CatalogOfferId(this.id),
            catalogOfferType = CatalogOfferType("COUPON"),
            price = Price.zero(),
            validity = this.validity,
            offerItems = listOf(
                Item.OfferItem(
                    productId = productId,
                    price = Price.zero(),
                    catalogOfferItemId = CatalogOfferItemId(this.compositionId),
                    recurrent = false
                )
            ),
            customFields = CustomFields(),
            offerFields = OfferFields()
        )

}
