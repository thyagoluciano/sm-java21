package br.com.zup.realwave.sales.manager.infrastructure.common.exception

import br.com.zup.realwave.common.exception.handler.to.ErrorCode

/**
 * Created by marcosgm on 30/05/17.
 */
class PurchaseOrderErrorCode private constructor(code: String, key: String) : ErrorCode(code, key) {
    companion object {
        val PURCHASE_ORDER_CHECKOUT_ERROR = PurchaseOrderErrorCode("SLM-010", "purchase.order.checkout.error")
        val PURCHASE_ORDER_NOT_EXISTS = PurchaseOrderErrorCode("PURCHASE_ORDER_NOT_EXISTS", "purchase.not.exists")
        val PURCHASE_ORDER_CANNOT_DELETE = PurchaseOrderErrorCode("PURCHASE_ORDER_CANNOT_DELETE", "purchase.not.delete")
        val CATALOG_SEARCH_INTEGRATION_ERROR =
            PurchaseOrderErrorCode("CATALOG_MANAGER_INTEGRATION_ERROR", "catalog.search.integration.error")
        val EVENT_STORE_INTEGRATION_ERROR =
            PurchaseOrderErrorCode("EVENT_STORE_INTEGRATION_ERROR", "event.store.integration.error")
        val CUSTOMER_INFO_INTEGRATION_ERROR: ErrorCode =
            PurchaseOrderErrorCode("CUSTOMER_INFO_INTEGRATION_ERROR", "customer.info.integration.error")
        val CUSTOMER_ORDER_MANAGER_INTEGRATION_ERROR = PurchaseOrderErrorCode(
            "CUSTOMER_ORDER_MANAGER_INTEGRATION_ERROR",
            "customer.order.manger.integration.error"
        )
        val CUSTOMER_NOT_FOUND = PurchaseOrderErrorCode("CUSTOMER_NOT_FOUND", "customer.not.found")
        val CUSTOMER_NOT_INFORMED = PurchaseOrderErrorCode("CUSTOMER_NOT_INFORMED", "customer.not.informed")
        val CUSTOMER_INACTIVE = PurchaseOrderErrorCode("CUSTOMER_INACTIVE", "customer.inactive")
        val PAYMENT_NOT_FOUND = PurchaseOrderErrorCode("PAYMENT_NOT_FOUND", "payment.not.found")
        val ALL_ITEMS_MUST_HAVE_PRODUCTID =
            PurchaseOrderErrorCode("ALL_ITEMS_MUST_HAVE_PRODUCTID", "purchase.items.must.have.productId.error")
        val PRICE_MUST_BE_INFORMED = PurchaseOrderErrorCode("PRICE_MUST_BE_INFORMED", "price.must.be.informed")
        val INCOMPATIBLE_PURCHASE_ORDER =
            PurchaseOrderErrorCode("INCOMPATIBLE_PURCHASE_ORDER", "incompatible.purchase.order")
        val PURCHASE_ORDER_INVALID_TYPE =
            PurchaseOrderErrorCode("PURCHASE_ORDER_INVALID_TYPE", "purchase.order.invalid.type")
        val PURCHASE_ORDER_TYPE_IS_ALREADY_DEFINED =
            PurchaseOrderErrorCode("PURCHASE_ORDER_TYPE_IS_ALREADY_DEFINED", "purchase.order.type.is.already.defined")
        val PURCHASE_ORDER_CALLBACK_INTEGRATION_ERROR = PurchaseOrderErrorCode(
            "PURCHASE_ORDER_CALLBACK_INTEGRATION_ERROR",
            "purchase.order.callback.integration.error"
        )
        val CATALOG_OFFER_NOT_RETURNED_BY_OFFER_DETAILS = PurchaseOrderErrorCode(
            "CATALOG_OFFER_NOT_RETURNED_BY_OFFER_DETAILS",
            "catalog.offer.not.returned.by.offer.details"
        )
        val CATALOG_OFFER_HAS_NO_DESCRIPTION =
            PurchaseOrderErrorCode("CATALOG_OFFER_HAS_NO_DESCRIPTION", "catalog.offer.has.no.description")
        val CATALOG_OFFER_NOT_RETURNED_BY_OFFER_ITEM_DETAILS = PurchaseOrderErrorCode(
            "CATALOG_OFFER_NOT_RETURNED_BY_OFFER_ITEM_DETAILS",
            "catalog.offer.not.returned.by.offer.item.details"
        )
        val PRODUCT_NOT_FOUND = PurchaseOrderErrorCode("PRODUCT_NOT_FOUND", "product.not.found")
        val PRODUCT_INACTIVE = PurchaseOrderErrorCode("PRODUCT_INACTIVE", "product.inactive")
        val COUPON_INTEGRATION_ERROR = PurchaseOrderErrorCode("COUPON_INTEGRATION_ERROR", "coupon.integration.error")
        val COUPON_VALIDATION_TYPE_ERROR =
            PurchaseOrderErrorCode("COUPON_VALIDATION_ERROR", "coupon.validation.type.error")
        val COUPON_NOT_FOUND = PurchaseOrderErrorCode("COUPON_NOT_FOUND", "coupon.not.found")
        val COUPON_DISCOUNT_NOT_INFORMED =
            PurchaseOrderErrorCode("COUPON_DISCOUNT_NOT_INFORMED", "coupon.discount.not.informed")
        val COUPON_INACTIVE = PurchaseOrderErrorCode("COUPON_INACTIVE", "coupon.inactive")
        val COUPON_SEGMENT_NOT_SUPPORTED =
            PurchaseOrderErrorCode("COUPON_SEGMENT_NOT_SUPPORTED", "coupon.segment.not.supported")
        val COUPON_REWARD_SERVICE_MISSING =
                PurchaseOrderErrorCode("COUPON_REWARD_SERVICE_MISSING", "coupon.reward.service.missing")
        val COMPOSITION_ID_INVALID = PurchaseOrderErrorCode("COMPOSITION_ID_INVALID", "composition.id.invalid")
        val CATALOG_SEARCH_OFFER_INVALID =
            PurchaseOrderErrorCode("CATALOG_SEARCH_OFFER_INVALID", "catalog.search.offer.invalid")
        val CATALOG_SEARCH_VALUE_ATTRIBUTE_ERROR =
            PurchaseOrderErrorCode("CATALOG_SEARCH_VALUE_ATTRIBUTE_ERROR", "catalog.search.value.attribute.error")
        val ITEMS_NOT_INFORMED = PurchaseOrderErrorCode("ITEMS_NOT_INFORMED", "items.not.informed")
        val SUBSCRIPTION_ID_NOT_INFORMED =
            PurchaseOrderErrorCode("SUBSCRIPTION_ID_NOT_INFORMED", "subscription.id.not.informed")
        val NO_NEED_PRODUCT_ID_TO_TYPE_JOIN =
            PurchaseOrderErrorCode("NO_NEED_PRODUCT_ID_TO_TYPE_JOIN", "no.need.product.id.to.type.join")
        val MORE_ONE_PAYMENT_METHOD =
                PurchaseOrderErrorCode("MORE_ONE_PAYMENT_METHOD","purchase.methods.more.one")
    }
}
