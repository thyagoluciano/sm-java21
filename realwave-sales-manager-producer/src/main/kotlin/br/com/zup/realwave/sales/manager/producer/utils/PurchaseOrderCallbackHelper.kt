package br.com.zup.realwave.sales.manager.producer.utils

import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.totalPrice
import br.com.zup.realwave.sales.manager.events.PurchaseOrderChangeEvent
import br.com.zup.realwave.sales.manager.events.PurchaseOrderChangeEvent.Price as EventPrice

private fun toEventPrice(price: Price): EventPrice = EventPrice(
    amount = price.amount,
    scale = price.scale,
    currency = price.currency
)

fun PurchaseOrder.toStateChange() =
    PurchaseOrderChangeEvent(
        event = PurchaseOrderChangeEvent.Event(
            purchaseOrder = PurchaseOrderChangeEvent.PurchaseOrder(
                purchaseOrderId = idAsString(),
                segmentation = PurchaseOrderChangeEvent.Segmentation(segmentation?.query),
                onBoardingSale = PurchaseOrderChangeEvent.OnBoardingSale(
                    offer = PurchaseOrderChangeEvent.OnBoardingSale.CatalogOfferId(onBoardingSale?.offer?.value),
                    customFields = onBoardingSale?.customFields
                ),
                mgm = PurchaseOrderChangeEvent.Mgm(mgm?.code, mgm?.customFields),
                customer = PurchaseOrderChangeEvent.Customer(customer?.id),
                coupon = PurchaseOrderChangeEvent.CouponCode(
                    code = coupon?.code,
                    customFields = coupon?.customFields,
                    description = coupon?.description,
                    reward = PurchaseOrderChangeEvent.CouponCode.Reward(
                        type = coupon?.reward?.type,
                        discounts = coupon?.reward?.discounts?.map {
                            PurchaseOrderChangeEvent.CouponCode.Reward.Discount(
                                segment = PurchaseOrderChangeEvent.CouponCode.Reward.Discount.Segment(
                                    id = it.segment?.id,
                                    name = it.segment?.name,
                                    type = it.segment?.type
                                ),
                                discount = PurchaseOrderChangeEvent.Price(
                                    currency = it.discount?.currency,
                                    amount = it.discount?.amount,
                                    scale = it.discount?.scale
                                ),
                                discountAsPercent = it.discountAsPercent
                            )
                        }
                    )
                ),
                totalPrice = toEventPrice(this.totalPrice()),
                payment = PurchaseOrderChangeEvent.Payment(
                    methods = payment.methods.map {
                        PurchaseOrderChangeEvent.Payment.PaymentMethod(
                            method = it.method,
                            methodId = it.methodId,
                            price = PurchaseOrderChangeEvent.Price(
                                currency = it.price?.currency,
                                amount = it.price?.amount,
                                scale = it.price?.scale
                            ),
                            customFields = it.customFields
                        )
                    },
                    description = PurchaseOrderChangeEvent.Payment.Description(value = payment.description?.value)
                ),
                customerOrder = PurchaseOrderChangeEvent.CustomerOrder(
                    customerOrderId = customerOrder?.customerOrderId,
                    status = customerOrder?.status,
                    steps = customerOrder?.steps?.map {
                        PurchaseOrderChangeEvent.Step(
                            step = it.step,
                            status = it.status,
                            startedAt = it.startedAt,
                            endedAt = it.endedAt,
                            processed = it.processed,
                            total = it.total
                        )
                    }
                ),
                installationAttributes = installationAttributes.entries.map {
                    PurchaseOrderChangeEvent.InstallationAttribute(
                        PurchaseOrderChangeEvent.ProductTypeId(it.key.value),
                        it.value.attributes
                    )
                },
                items = items.map { item ->
                    PurchaseOrderChangeEvent.Item(
                        id = PurchaseOrderChangeEvent.Item.Id(value = item.id.value),
                        catalogOfferId = PurchaseOrderChangeEvent.CatalogOfferId(value = item.catalogOfferId.value),
                        catalogOfferType = PurchaseOrderChangeEvent.CatalogOfferType(value = item.catalogOfferType.value),
                        price = PurchaseOrderChangeEvent.Price(
                            currency = item.price.currency,
                            amount = item.price.amount,
                            scale = item.price.scale
                        ),
                        validity = PurchaseOrderChangeEvent.OfferValidity(
                            period = item.validity.period,
                            duration = item.validity.duration,
                            unlimited = item.validity.unlimited
                        ),
                        offerFields = PurchaseOrderChangeEvent.OfferFields(
                            value = item.offerFields.value
                        ),
                        customFields = PurchaseOrderChangeEvent.CustomFields(
                            value = item.customFields.value
                        ),
                        offerItems = item.offerItems.map { offerItem ->
                            PurchaseOrderChangeEvent.Item.OfferItem(
                                productId = PurchaseOrderChangeEvent.ProductId(
                                    value = offerItem.productId?.value
                                ),
                                catalogOfferItemId = PurchaseOrderChangeEvent.Item.OfferItem.CatalogOfferItemId(
                                    value = offerItem.catalogOfferItemId.value
                                ),
                                price = PurchaseOrderChangeEvent.Price(
                                    currency = offerItem.price.currency,
                                    amount = offerItem.price.amount,
                                    scale = offerItem.price.scale
                                ),
                                recurrent = offerItem.recurrent,
                                customFields = PurchaseOrderChangeEvent.CustomFields(
                                    value = offerItem.customFields?.value
                                ),
                                userParameters = offerItem.userParameters
                            )
                        }
                    )
                }.toMutableSet(),
                createdAt = createdAt,
                updatedAt = updatedAt,
                protocol = protocol,
                type = type?.name,
                subscriptionId = subscriptionId,
                channelCreate = PurchaseOrderChangeEvent.Channel(
                    value = channelCreate?.value
                ),
                channelCheckout = PurchaseOrderChangeEvent.Channel(
                    value = channelCheckout?.value
                ),
                callback = PurchaseOrderChangeEvent.Callback(
                    url = callback?.url,
                    headers = callback?.headers
                ),
                reason = PurchaseOrderChangeEvent.Reason(
                    code = reason?.code,
                    description = reason?.description
                ),
                securityCodeInformed = securityCodeInformed?.map {
                    PurchaseOrderChangeEvent.SecurityCodeInformed(
                        methodId = it.methodId,
                        securityCodeInformed = it.securityCodeInformed
                    )
                },
                status = PurchaseOrderChangeEvent.PurchaseOrderStatus.valueOf(
                    value = status.name
                ),
                salesForce = PurchaseOrderChangeEvent.SalesForce(
                    id = salesForce?.id,
                    name = salesForce?.name
                )
            )
        )
    )
