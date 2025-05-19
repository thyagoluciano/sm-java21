package br.com.zup.realwave.sales.manager.events

import com.fasterxml.jackson.databind.JsonNode

data class PurchaseOrderChangeEvent(val event: Event) {

    data class Event(val purchaseOrder: PurchaseOrder)

    data class PurchaseOrder(
        var purchaseOrderId: String,
        var segmentation: Segmentation?,
        var onBoardingSale: OnBoardingSale?,
        var mgm: Mgm?,
        var customer: Customer?,
        var coupon: CouponCode?,
        var totalPrice: Price?,
        var payment: Payment,
        var customerOrder: CustomerOrder?,
        var installationAttributes: List<InstallationAttribute>,
        var items: MutableSet<Item>,
        var createdAt: String?,
        var updatedAt: String?,
        var protocol: String?,
        var type: String?,
        var subscriptionId: String?,
        var channelCreate: Channel?,
        var channelCheckout: Channel?,
        var callback: Callback?,
        var reason: Reason?,
        var securityCodeInformed: List<SecurityCodeInformed>?,
        var status: PurchaseOrderStatus,
        var salesForce: SalesForce?
    )

    data class Segmentation(val query: JsonNode?)

    data class OnBoardingSale(val offer: CatalogOfferId, var customFields: JsonNode?) {

        data class CatalogOfferId(val value: String?)

    }

    data class Mgm(val code: String?, var customFields: JsonNode?)

    data class Customer(val id: String?)

    data class CouponCode(
        val code: String?,
        var customFields: JsonNode?,
        val description: String?,
        val reward: Reward?
    ) {

        data class Reward(val type: String?, val discounts: List<Discount>?) {

            data class Discount(
                val segment: Segment?,
                val discount: Price?,
                val discountAsPercent: Int?
            ) {

                data class Segment(val id: String?, val name: String?, val type: String?)

            }

        }

    }

    data class Price(val currency: String?, val amount: Int?, val scale: Int?)

    data class Payment(var methods: List<PaymentMethod>, val description: Description?) {

        data class PaymentMethod(
            val method: String,
            val methodId: String?,
            val price: Price?,
            val customFields: JsonNode?,
            val securityCodeInformed: Boolean = false
        )

        data class Description(val value: String?)

    }

    data class CustomerOrder(val customerOrderId: String?, val status: String?, val steps: List<Step>?)

    data class Step(
        val step: String?,
        val status: String?,
        val startedAt: String?,
        val endedAt: String?,
        val processed: Int?,
        val total: Int?
    )

    data class ProductTypeId(val value: String)

    data class InstallationAttribute(val productTypeId: ProductTypeId, val attributes: Map<String, Any>)

    data class Item(
        val id: Id,
        val catalogOfferId: CatalogOfferId,
        val catalogOfferType: CatalogOfferType,
        val price: Price,
        val validity: OfferValidity,
        val offerFields: OfferFields,
        val customFields: CustomFields,
        val offerItems: List<OfferItem>
    ) {

        data class Id(val value: String)

        data class OfferItem(
            val productId: ProductId?,
            val catalogOfferItemId: CatalogOfferItemId,
            val price: Price,
            val recurrent: Boolean = false,
            val customFields: CustomFields?,
            val userParameters: Map<String, Any>?
        ) {

            data class CatalogOfferItemId(val value: String)

        }

    }

    data class CatalogOfferId(val value: String)

    data class CatalogOfferType(val value: String)

    data class OfferValidity(
        val period: String?,
        val duration: Int?,
        val unlimited: Boolean
    )

    data class OfferFields(val value: JsonNode?)

    data class CustomFields(val value: JsonNode?)

    data class ProductId(val value: String?)

    data class Channel(val value: String?)

    data class Callback(val url: String?, val headers: JsonNode?)

    data class Reason(val code: String?, val description: String?)

    data class SecurityCodeInformed(val methodId: String, val securityCodeInformed: Boolean)

    enum class PurchaseOrderStatus {
        OPENED,
        CHECKED_OUT,
        COMPLETED,
        FAILED,
        CANCELED,
        DELETED;
    }

    data class SalesForce(val id: String?, val name: String?)

}
