package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.AggregateVersion
import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.ProductId
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.RescueServiceCoupon
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCustomerUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderInstallationAttributesUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderItemAdded
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderPaymentUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderStatusUpdated
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSubscriptionUpdated
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

fun buildPurchaseOrder(
    items: List<Item> = listOf(purchaseOrderItem()),
    purchaseOrderId: PurchaseOrderId = PurchaseOrderId(),
    installationAttribute: InstallationAttribute = installationAttribute(
        UUID.randomUUID().toString()
    ),
    type: PurchaseOrderType? = null,
    subscription: String = "",
    status: PurchaseOrderStatus = PurchaseOrderStatus.OPENED
): PurchaseOrder {

    val purchaseOrder = PurchaseOrder()

    purchaseOrder.load(
        listOf(
            PurchaseOrderCreated(purchaseOrderId, type),
            PurchaseOrderSegmentationUpdated(purchaseOrderId, segmentation()),
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

fun purchaseOrderCouponMoney(type: String = "DISCOUNT_MONEY", amount: Int = 1000) = CouponCode(
    code = "code",
    reward = CouponCode.Reward(
        type = type,
        discounts = listOf(
            CouponCode.Discount(
                discount = Price(
                    amount = amount,
                    currency = "BRL",
                    scale = 2
                )
            )
        )
    )
)

fun purchaseOrderCouponPercent(type: String = "DISCOUNT_PERCENT", discountAsPercent: Int = 10) = CouponCode(
    code = "code",
    reward = CouponCode.Reward(
        type = type,
        discounts = listOf(
            CouponCode.Discount(
                discountAsPercent = discountAsPercent
            )
        )
    )
)

fun purchaseOrderItem(
    id: Item.Id = Item.Id(),
    catalogOfferId: CatalogOfferId = CatalogOfferId(UUID.randomUUID().toString()),
    type: String = "CUSTOM_PLAN",
    offerItems: List<Item.OfferItem> = customPlanOfferItems(),
    validity: OfferValidity = OfferValidity("DAY", 30, false),
    pricesPerPeriod: List<PricePerPeriod> = pricesPerPeriod()
) =
    Item(
        id = id,
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
        offerItems = offerItems,
        pricesPerPeriod = pricesPerPeriod
    )

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

fun planOfferItems(): List<Item.OfferItem> {
    return listOf(
        offerItem(
            "plan-item-1",
            Price(
                amount = 1001,
                currency = "BRL",
                scale = 2
            )
        ),
        offerItem(
            "plan-item-2",
            Price(
                amount = 1002,
                currency = "BRL",
                scale = 2
            )
        )
    )
}

fun rescueCoupon() = RescueServiceCoupon(
    id = "couponCode-id",
    code = "code",
    validity = OfferValidity(
        period = "DAY",
        unlimited = false,
        duration = 30
    ),
    compositionId = "composition-id"
)

fun createPurchaseItemCoupon(coupon: RescueServiceCoupon) = coupon.createPurchaseOrderItem(ProductId("produc-id"))

fun customPlanOfferItems(
    catalogOfferItemId: String,
    price: Price = Price(amount = 1001, currency = "BRL", scale = 2)
): List<Item.OfferItem> {
    return listOf(offerItem(catalogOfferItemId, price))
}

fun offerItemWithProductId(
    catalogOfferItemId: String = "test-offer-item-1",
    catalogOfferItemName: String = "item-name",
    price: Price = Price(
        amount = 1000,
        currency = "BRL",
        scale = 2
    )
): List<Item.OfferItem> {
    return listOf(
        Item.OfferItem(
            productId = ProductId("4B342980-0788-4BA7-B4A4-B655946519CE"),
            catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(catalogOfferItemId),
            price = price,
            recurrent = true
        ),
        Item.OfferItem(
            productId = ProductId("4B342980-0788-4BA7-B4A4-B655946519CE"),
            catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(catalogOfferItemId),
            price = price,
            recurrent = true
        )
    )
}

fun offersItems(): List<Item.OfferItem> {
    return listOf(
        offerItem(
            "plan-item-1",
            Price(
                amount = 1001,
                currency = "BRL",
                scale = 2
            )
        ),
        offerItem(
            "plan-item-2",
            Price(
                amount = 1002,
                currency = "BRL",
                scale = 2
            )
        )
    )
}

fun offerItem(
    catalogOfferItemId: String = UUID.randomUUID().toString(),
    price: Price = Price(
        amount = 1000,
        currency = "BRL",
        scale = 2
    )
): Item.OfferItem {
    return Item.OfferItem(
        catalogOfferItemId = Item.OfferItem.CatalogOfferItemId(catalogOfferItemId),
        price = price,
        recurrent = true
    )
}

fun customPlanOfferItems(): List<Item.OfferItem> {
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

private fun randomUUID() =
    UUID.randomUUID().toString()
