package br.com.zup.realwave.sales.manager.integration


import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferName
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Item.OfferItem
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCouponUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID


fun buildPurchaseOrder(
    items: List<Item> = listOf(purchaseOrderItem()),
    purchaseOrderId: PurchaseOrderId = PurchaseOrderId(),
    type: PurchaseOrderType = PurchaseOrderType.JOIN,
    installationAttribute: InstallationAttribute = installationAttribute(
        UUID.randomUUID().toString()
    ),
    subscription: String = "",
    callbackUrl: Callback? = null,
    payment: Payment = Payment(
        listOf(
            Payment.PaymentMethod(
                method = "CREDIT_CARD",
                methodId = "payment-id"
            ),
            Payment.PaymentMethod(
                method = "ACCOUNT_CASH",
                methodId = "payment-id-1"
            ),
            Payment.PaymentMethod(
                method = "ACCOUNT_CASH",
                methodId = "account_id"
            )
        )
    ),
    freight: Freight? = null
): PurchaseOrder {

    val purchaseOrder = PurchaseOrder()

    purchaseOrder.load(
        listOf(
            PurchaseOrderCreated(purchaseOrderId, purchaseOrderType = type, callback = callbackUrl),
            PurchaseOrderSegmentationUpdated(purchaseOrderId, segmentation()),
            PurchaseOrderCustomerUpdated(purchaseOrderId, Customer(UUID.randomUUID().toString())),
            PurchaseOrderInstallationAttributesUpdated(
                aggregateId = purchaseOrderId,
                installationAttribute = installationAttribute
            ),
            PurchaseOrderPaymentUpdated(purchaseOrderId, payment),
            PurchaseOrderSubscriptionUpdated(purchaseOrderId, Subscription(subscription))
        ), AggregateVersion(6)
    )

    purchaseOrder.load(items.map { PurchaseOrderItemAdded(purchaseOrderId, it) }, AggregateVersion(10))

    if (freight != null)
        purchaseOrder.freight = freight

    return purchaseOrder
}


fun buildPurchaseOrderCoupon(
    purchaseOrderId: PurchaseOrderId = PurchaseOrderId(),
    item: Item = purchaseOrderItemWithProduct(),
    payment: Payment = Payment(listOf(Payment.PaymentMethod(method = "REWARD", methodId = "REWARD")))
): PurchaseOrder {

    val purchaseOrder = PurchaseOrder()

    purchaseOrder.loadEvents(
        listOf(
            PurchaseOrderCreated(
                aggregateId = purchaseOrderId,
                purchaseOrderType = PurchaseOrderType.COUPON,
                callback = null
            ),
            PurchaseOrderItemAdded(
                aggregateId = purchaseOrderId,
                item = item
            ),
            PurchaseOrderCouponUpdated(
                aggregateId = purchaseOrderId,
                coupon = CouponCode("COUPON-CODE")
            ),
            PurchaseOrderCustomerUpdated(
                aggregateId = purchaseOrderId,
                customer = Customer(UUID.randomUUID().toString())
            ),
            PurchaseOrderPaymentUpdated(
                aggregateId = purchaseOrderId,
                payment = payment
            )
        )
    )

    return purchaseOrder

}

fun purchaseOrderItem(
    catalogOfferId: CatalogOfferId = CatalogOfferId("9fb38ac3-10ee-460e-9772-358c0beaddd2"),
    catalogOfferName: CatalogOfferName = CatalogOfferName("CATALOG-NAME"),
    type: String = "CUSTOMPLAN",
    offerItems: List<OfferItem> = offerItems(),
    validity: OfferValidity = OfferValidity("DAY", 30, false),
    pricesPerPeriod: List<PricePerPeriod> = listOf()
) =
    Item(
        catalogOfferId = catalogOfferId,
        catalogOfferType = CatalogOfferType(type),
        price = Price(
            currency = "BRL",
            amount = 1100,
            scale = 2
        ),
        validity = validity,
        offerFields = OfferFields(null),
        customFields = CustomFields(ObjectMapper().createObjectNode()),
        offerItems = offerItems,
        pricesPerPeriod = pricesPerPeriod
    )

fun purchaseOrderItemWithProduct(
    catalogOfferId: CatalogOfferId = CatalogOfferId("9fb38ac3-10ee-460e-9772-358c0beaddd2"),
    type: String = "CUSTOMPLAN",
    validity: OfferValidity = OfferValidity("DAY", 30, false)
) =
    Item(
        catalogOfferId = catalogOfferId,
        catalogOfferType = CatalogOfferType(type),
        price = Price(
            currency = "BRL",
            amount = 1100,
            scale = 2
        ),
        validity = validity,
        offerFields = OfferFields(null),
        customFields = CustomFields(ObjectMapper().createObjectNode()),
        offerItems = purchaseOrderOfferItemsWithProduct()
    )

fun purchaseOrderOfferItemsWithProduct(
    catalogOfferItemId: OfferItem.CatalogOfferItemId = OfferItem.CatalogOfferItemId("65745bb1-ba36-4476-9e64-3201b7e29821")
) = listOf(
    OfferItem(
        productId = ProductId("14f1eda4-e223-407d-84a2-3e2871966c68"),
        catalogOfferItemId = catalogOfferItemId,
        price = Price(
            amount = 1000,
            currency = "BRL",
            scale = 2
        )
    )
)

fun offerItemWithProductId(
    catalogOfferItemId: String = "65745bb1-ba36-4476-9e64-3201b7e29821",
    catalogOfferItemName: String = "item-name",
    price: Price = Price(
        amount = 1000,
        currency = "BRL",
        scale = 2
    )
): List<OfferItem> {
    return listOf(
        OfferItem(
            productId = ProductId("4B342980-0788-4BA7-B4A4-B655946519CE"),
            catalogOfferItemId = OfferItem.CatalogOfferItemId(catalogOfferItemId),
            price = price,
            recurrent = true
        )
    )
}

fun offerItemWithoutOneProductId(
    catalogOfferItemId: String = "65745bb1-ba36-4476-9e64-3201b7e29821",
    catalogOfferItemName: String = "item-name",
    price: Price = Price(
        amount = 1000,
        currency = "BRL",
        scale = 2
    )
): List<OfferItem> {
    return listOf(
        OfferItem(
            productId = ProductId("4B342980-0788-4BA7-B4A4-B655946519CE"),
            catalogOfferItemId = OfferItem.CatalogOfferItemId(catalogOfferItemId),
            price = price,
            recurrent = true
        ),
        OfferItem(
            catalogOfferItemId = OfferItem.CatalogOfferItemId(catalogOfferItemId),
            price = price,
            recurrent = true
        )
    )
}

fun planOfferItems(): List<OfferItem> {
    return listOf(
        offerItem(
            "990ddd1a-8ce4-4ec8-88f9-06968e972dd1",
            "plan-item-1",
            Price(
                amount = 100,
                currency = "BRL",
                scale = 2
            ), true,
            mapOf<String, Any>("test" to "test")
        ),
        offerItem(
            "c68931dd-fe4e-4cc3-a8dd-ec77f3f3eeb0",
            "plan-item-2",
            Price(
                amount = 1000,
                currency = "BRL",
                scale = 2
            ), true
        )
    )
}

fun offerItems(
    catalogOfferItemId: String,
    catalogOfferItemName: String,
    price: Price = Price(amount = 1001, currency = "BRL", scale = 2), recurrent: Boolean
): List<OfferItem> {
    return listOf(offerItem(catalogOfferItemId, catalogOfferItemName, price, recurrent))
}

fun offerItem(
    catalogOfferItemId: String = UUID.randomUUID().toString(),
    catalogOfferItemName: String = "item-name",
    price: Price = Price(
        amount = 1000,
        currency = "BRL",
        scale = 2
    ), recurrent: Boolean,
    userParameters: Map<String, Any>? = null
): OfferItem {
    return OfferItem(
        catalogOfferItemId = OfferItem.CatalogOfferItemId(catalogOfferItemId),
        price = price,
        recurrent = recurrent,
        userParameters = userParameters
    )
}

fun offersItems(): List<OfferItem> {
    return listOf(
        offerItem(
            "65745bb1-ba36-4476-9e64-3201b7e29821",
            "test-offer-item-1",
            Price(
                amount = 1001,
                currency = "BRL",
                scale = 2
            ), true
        )
    )
}

fun pricesPerPeriod() =
    listOf(
        PricePerPeriod(
            totalPrice = Price("BRL", 1000, 2),
            totalDiscountPrice = Price("BRL", 800, 2),
            totalPriceWithDiscount = Price("BRL", 200, 2),
            startAt = PricePerPeriod.StartAt(1),
            endAt = PricePerPeriod.EndAt(2),
            items = listOf()
        )
    )


fun offerItems(): List<OfferItem> {
    return listOf(
        OfferItem(
            catalogOfferItemId = OfferItem.CatalogOfferItemId("65745bb1-ba36-4476-9e64-3201b7e29821"),
            price = Price(amount = 1000, currency = "BRL", scale = 2)
        )
    )
}


fun segmentation(): Segmentation {
    val query = """
    {
   "filter":{
      "customerId":"11111111-1111-1111-1111-111111111111",
      "mergeCatalogs":true,
      "profile":{
         "type":"CLAUSE",
         "logicalOperator":"AND",
         "clauses":[
            {
               "type":"RULE",
               "content":{
                  "key":"gender",
                  "condition":"EQUAL",
                  "value":[
                     "M"
                  ]
               }
            },
            {
               "type":"RULE",
               "content":{
                  "key":"tags.tag",
                  "condition":"EQUAL",
                  "value":[
                     "CLIENT"
                  ]
               }
            }
         ]
      },
      "catalog":{
         "type":"CLAUSE",
         "logicalOperator":"OR",
         "clauses":[
            {
               "type":"RULE",
               "content":{
                  "key":"channels.name",
                  "condition":"EQUAL",
                  "value":[
                     "CHANNEL1",
                     "AAA"
                  ]
               }
            },
            {
               "type":"RULE",
               "content":{
                  "key":"channels.name",
                  "condition":"EQUAL",
                  "value":[
                     "CHANNEL2",
                     "AAA"
                  ]
               }
            }
         ]
      }
   }
}
    """
    return Segmentation(query = query.jsonToObject(JsonNode::class.java))
}

fun installationAttribute(
    productTypeId: String, attributes: Map<String, Any> = mapOf(
        "param1" to "value1",
        "param2" to "value2"
    )
) =
    InstallationAttribute(ProductTypeId(productTypeId), attributes)

fun PurchaseOrder.loadEvents(events: List<Event>) {
    for (event: Event in events) {
        applyChange(event)
    }
}

fun buildPurchaseOrderWithBoleto(
    items: List<Item> = listOf(purchaseOrderItem()),
    purchaseOrderId: PurchaseOrderId = PurchaseOrderId(),
    type: PurchaseOrderType = PurchaseOrderType.JOIN,
    installationAttribute: InstallationAttribute = installationAttribute(
        UUID.randomUUID().toString()
    ),
    subscription: String = "",
    callbackUrl: Callback? = null,
    payment: Payment = Payment(
        listOf(
            Payment.PaymentMethod(
                method = "BOLETO",
                methodId = null
            )
        )
    )
): PurchaseOrder {

    val purchaseOrder = PurchaseOrder()

    purchaseOrder.load(
        listOf(
            PurchaseOrderCreated(purchaseOrderId, purchaseOrderType = type, callback = callbackUrl),
            PurchaseOrderSegmentationUpdated(purchaseOrderId, segmentation()),
            PurchaseOrderCustomerUpdated(purchaseOrderId, Customer(UUID.randomUUID().toString())),
            PurchaseOrderInstallationAttributesUpdated(
                aggregateId = purchaseOrderId,
                installationAttribute = installationAttribute
            ),
            PurchaseOrderPaymentUpdated(purchaseOrderId, payment),
            PurchaseOrderSubscriptionUpdated(purchaseOrderId, Subscription(subscription))
        ), AggregateVersion(6)
    )

    purchaseOrder.load(items.map { PurchaseOrderItemAdded(purchaseOrderId, it) }, AggregateVersion(10))

    return purchaseOrder
}
