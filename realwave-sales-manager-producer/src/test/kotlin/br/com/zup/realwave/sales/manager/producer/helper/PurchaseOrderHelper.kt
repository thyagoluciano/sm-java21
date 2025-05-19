package br.com.zup.realwave.sales.manager.producer.helper

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

fun samplePurchaseOrder(
    items: List<Item> = listOf(purchaseOrderItem()),
    purchaseOrderId: PurchaseOrderId = PurchaseOrderId(),
    installationAttribute: InstallationAttribute = installationAttribute(
        UUID.randomUUID().toString()
    ),
    type: PurchaseOrderType? = PurchaseOrderType.JOIN,
    subscription: String = "",
    status: PurchaseOrderStatus = PurchaseOrderStatus.OPENED
): PurchaseOrder {

    var purchaseOrder : PurchaseOrder = PurchaseOrder()

    purchaseOrder.load(
        listOf(
            PurchaseOrderCreated(purchaseOrderId, type),
            PurchaseOrderCustomerUpdated(purchaseOrderId, Customer(UUID.randomUUID().toString())),
            PurchaseOrderInstallationAttributesUpdated(
                aggregateId = purchaseOrderId,
                installationAttribute = installationAttribute
            ),
            PurchaseOrderPaymentUpdated(
                purchaseOrderId,
                Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id")))
            ),
            PurchaseOrderSubscriptionUpdated(purchaseOrderId, Subscription(subscription)),
            PurchaseOrderStatusUpdated(purchaseOrderId, status)

        ), AggregateVersion(6)
    )

    purchaseOrder.load(items.map { PurchaseOrderItemAdded(purchaseOrderId, it) }, AggregateVersion(10))

    return purchaseOrder

}

fun purchaseOrderItem(
    catalogOfferId: CatalogOfferId = CatalogOfferId(UUID.randomUUID().toString()),
    type: String = "PLAN",
    offerItems: List<Item.OfferItem> = planOfferItems(),
    validity: OfferValidity = OfferValidity("DAY", 30, false)
) =
    Item(
        catalogOfferId = catalogOfferId,
        catalogOfferType = CatalogOfferType(type),
        price = Price(
            currency = "BRL",
            amount = 3799,
            scale = 2
        ),
        validity = validity,
        offerFields = OfferFields(null),
        customFields = CustomFields(ObjectMapper().createObjectNode()),
        offerItems = offerItems
    )

fun planOfferItems(): List<Item.OfferItem> {
    return listOf(
        Item.OfferItem(
            catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(UUID.randomUUID().toString()),
            price = Price(
                amount = 1000,
                currency = "BRL",
                scale = 2
            )
        )
    )
}

fun installationAttribute(
    productTypeId: String, attributes: Map<String, Any> = mapOf(
        "param1" to "value1",
        "param2" to "value2"
    )
) =
    InstallationAttribute(ProductTypeId(productTypeId), attributes)

