package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferRepresentation
import br.com.zup.realwave.common.exception.handler.to.ErrorCode
import br.com.zup.realwave.pcm.commonsmodel.representation.CompositionRepresentation
import br.com.zup.realwave.sales.manager.domain.Boleto
import br.com.zup.realwave.sales.manager.domain.CouponCode.Discount
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.SecurityCode
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.discountPrice
import br.com.zup.realwave.sales.manager.domain.installationAttributesFor
import br.com.zup.realwave.sales.manager.infrastructure.Validator
import br.com.zup.realwave.sales.manager.integration.feign.extensions.getOfferCompositionRepresentation
import br.com.zup.realwave.sales.manager.integration.feign.extensions.getOfferNameDescription
import br.com.zup.realwave.sales.manager.integration.feign.extensions.getProductTypeList
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import feign.Headers
import feign.RequestLine
import org.hibernate.validator.constraints.NotBlank

/**
 * Created by cleber on 6/8/17.
 */
@Headers("Accept: application/json")
interface CustomerOrderManagerApiService {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /customer-orders")
    fun checkoutPurchaseOrder(customerOrderRequest: CustomerOrderRequest?): CustomerOrderResponse

    data class CustomerOrderRequest(
        val customerId: String,
        val products: List<CustomerOrderRequestProduct>,
        val offers: List<CustomerOrderRequestOffer>,
        val externalId: String,
        val externalModule: String,
        val callback: String,
        val payment: CustomerOrderRequestPayment,
        val type: String,
        val mgm: Mgm?,
        val subscriptionId: String?,
        val coupon: CouponCode?,
        val freight: CustomerOrderRequestFreight?
    ) {
        companion object {
            fun of(
                purchaseOrder: PurchaseOrder,
                offersDetailsResponse: OfferRepresentation,
                callbackUrl: String,
                externalModule: String,
                securityCodes: List<SecurityCode>?
            ): CustomerOrderRequest {
                val customerOrderRequest = CustomerOrderRequest(
                    customerId = purchaseOrder.customer?.id!!,
                    externalId = purchaseOrder.idAsString(),
                    callback = callbackUrl,
                    products = buildProducts(offersDetailsResponse, purchaseOrder),
                    offers = buildOffers(offersDetailsResponse, purchaseOrder),
                    payment = buildPayments(purchaseOrder, securityCodes),
                    type = purchaseOrder.type!!.name,
                    mgm = Mgm(purchaseOrder.mgm?.code),
                    externalModule = externalModule,
                    subscriptionId = purchaseOrder.subscriptionId,
                    coupon = buildCouponCode(purchaseOrder),
                    freight = buildFreitht(purchaseOrder.freight)
                )
                return RealwaveCatalogManagerAdapter.adapt(customerOrderRequest)
            }

            fun of(
                purchaseOrder: PurchaseOrder,
                compositionResponse: CompositionRepresentation,
                callbackUrl: String,
                externalModule: String,
                securityCodes: List<SecurityCode>?
            ): CustomerOrderRequest {
                val customerOrderRequest = CustomerOrderRequest(
                    customerId = purchaseOrder.customer?.id!!,
                    externalId = purchaseOrder.idAsString(),
                    callback = callbackUrl,
                    products = buildProductsCoupon(purchaseOrder, compositionResponse),
                    offers = buildOffersCoupon(compositionResponse, purchaseOrder),
                    payment = buildPayments(purchaseOrder, securityCodes),
                    type = purchaseOrder.type!!.name,
                    mgm = Mgm(purchaseOrder.mgm?.code),
                    externalModule = externalModule,
                    subscriptionId = purchaseOrder.subscriptionId,
                    coupon = buildCouponCode(purchaseOrder),
                    freight = buildFreitht(purchaseOrder.freight)
                )
                return RealwaveCatalogManagerAdapter.adapt(customerOrderRequest)
            }


            private fun buildFreitht(freight: Freight?): CustomerOrderRequestFreight? {
                if (freight == null) return null

                return CustomerOrderRequestFreight(
                    type = freight.type.value,
                    price = freight.price,
                    address = CustomerOrderRequestFreight.Address(
                        city = freight.address.city,
                        number = freight.address.number,
                        complement = freight.address.complement,
                        country = freight.address.country,
                        district = freight.address.district,
                        name = freight.address.name,
                        state = freight.address.state,
                        street = freight.address.street,
                        zipCode = freight.address.zipCode
                    ),
                    deliveryTotalTime = freight.deliveryTotalTime.value
                )
            }

            private fun buildCouponCode(purchaseOrder: PurchaseOrder): CouponCode? {
                return if (null == purchaseOrder.coupon?.code) {
                    null
                } else {
                    CouponCode(
                        code = purchaseOrder.coupon!!.code,
                        discounts = buildDiscounts(purchaseOrder)
                    )
                }
            }

            private fun buildDiscounts(purchaseOrder: PurchaseOrder): List<CouponDiscount>? {
                return purchaseOrder.coupon!!.reward?.discounts?.let {
                    it.map {
                        CouponDiscount(
                            description = buildDescription(purchaseOrder, it),
                            price = purchaseOrder.discountPrice()
                        )
                    }
                }
            }

            private fun buildDescription(purchaseOrder: PurchaseOrder, discount: Discount): String {
                return if (discount.segment?.name != null) {
                    purchaseOrder.coupon!!.code + " " + discount.segment!!.name
                } else {
                    purchaseOrder.coupon!!.code + " " + purchaseOrder.coupon!!.description
                }
            }

            private fun buildOffersCoupon(
                compositionResponse: CompositionRepresentation,
                purchaseOrder: PurchaseOrder
            ): List<CustomerOrderRequestOffer> {
                val item = purchaseOrder.items.first()
                return listOf(
                    CustomerOrderRequestOffer(
                        catalogOfferDescription = compositionResponse.description!!.description,
                        catalogOfferId = item.catalogOfferId.value,
                        catalogOfferName = compositionResponse.name.name,
                        catalogOfferType = item.catalogOfferType.value,
                        price = Price.zero(),
                        validity = item.validity,
                        offerItems = listOf(
                            CustomerOrderRequestOfferItem(
                                catalogOfferItemId = item.offerItems.first().catalogOfferItemId.value,
                                compositionId = compositionResponse.id.id,
                                compositionName = compositionResponse.name.name,
                                price = Price.zero(),
                                productTypeName = compositionResponse.component!!.productType!!.name.name,
                                productTypeId = compositionResponse.component!!.productType!!.id.id,
                                productId = item.offerItems.first().productId!!.value,
                                recurrent = false
                            )
                        ),
                        pricesPerPeriod = item.pricesPerPeriod.map { buildPricesPerPeriod(it) },
                        quantity = item.quantity.value
                    )
                )
            }

            private fun buildProductsCoupon(
                purchaseOrder: PurchaseOrder,
                compositionResponse: CompositionRepresentation
            ): List<CustomerOrderRequestProduct> {
                return listOf(
                    CustomerOrderRequestProduct(
                        productId = purchaseOrder.items.first().offerItems.first().productId!!.value,
                        productTypeId = compositionResponse.component!!.productType!!.id.id,
                        productTypeName = compositionResponse.component!!.productType!!.name.name,
                        installationAttributes = mapOf()
                    )
                )
            }

            private fun buildPayments(
                purchaseOrder: PurchaseOrder,
                securityCodes: List<SecurityCode>?
            ): CustomerOrderRequestPayment {
                return CustomerOrderRequestPayment(
                    methods = purchaseOrder.payment.methods.map {
                        PaymentMethodRequest(
                            method = it.method,
                            methodId = it.methodId,
                            price = it.price,
                            customFields = it.customFields,
                            securityCode = it.methodId?.let { getSecurityCode(securityCodes, it) },
                            installments = it.installments
                        )
                    },
                    description = purchaseOrder.payment.description?.value,
                    async = purchaseOrder.isMethodBoletoOrAsync()
                )
            }

            private fun PurchaseOrder.isMethodBoletoOrAsync(): Boolean = payment.methods.any {
                it.method == "BOLETO" || payment.async ?: false
            }

            private fun getSecurityCode(securityCodes: List<SecurityCode>?, methodId: String) =
                securityCodes?.firstOrNull { it.methodId == methodId }?.securityCode

            private fun buildProducts(
                offersDetailsResponse: OfferRepresentation,
                purchaseOrder: PurchaseOrder
            ): List<CustomerOrderRequestProduct> {
                val productTypeList = offersDetailsResponse.getProductTypeList()

                return productTypeList.distinct().map { productType ->
                    CustomerOrderRequestProduct(
                        productTypeId = productType.first.id,
                        productTypeName = productType.second.name,
                        installationAttributes = purchaseOrder.installationAttributesFor(productType.first.id)
                    )
                }
            }

            private fun buildOffers(
                offersDetailsResponse: OfferRepresentation,
                purchaseOrder: PurchaseOrder
            ): List<CustomerOrderRequestOffer> {

                return purchaseOrder.items.map { item ->

                    val (name, description) = offersDetailsResponse.getOfferNameDescription(
                        item.catalogOfferId.value,
                        item.catalogOfferType,
                        item.offerItems
                    )
                    CustomerOrderRequestOffer(
                        catalogOfferId = item.catalogOfferId.value,
                        catalogOfferType = item.catalogOfferType.value,
                        catalogOfferName = name,
                        catalogOfferDescription = description ?: name,
                        price = item.price,
                        validity = item.validity,
                        offerItems = buildOfferItems(item, offersDetailsResponse),
                        pricesPerPeriod = item.pricesPerPeriod.map { buildPricesPerPeriod(it) },
                        quantity = item.quantity.value
                    )
                }
            }

            private fun buildOfferItems(
                item: Item,
                offersDetailsResponse: OfferRepresentation
            ): List<CustomerOrderRequestOfferItem> {
                return item.offerItems.map { (productId, catalogOfferItemId, price, recurrent, _, userParameters) ->
                    val offerCompositionRepresentation =
                        offersDetailsResponse.getOfferCompositionRepresentation(
                            catalogOfferItemId.value,
                            item.catalogOfferType
                        )!!
                    CustomerOrderRequestOfferItem(
                        catalogOfferItemId = catalogOfferItemId.value,
                        price = price,
                        recurrent = recurrent,
                        productTypeId = offerCompositionRepresentation.component.productType.id.id,
                        productId = productId?.value,
                        productTypeName = offerCompositionRepresentation.component.productType.name.name,
                        compositionId = offerCompositionRepresentation.id.id,
                        compositionName = offerCompositionRepresentation.name.name,
                        userParameters = userParameters
                    )
                }

            }

            private fun buildPricesPerPeriod(pricesPerPeriod: PricePerPeriod) =
                CustomerOrderRequestPricesPerPeriod(
                    startAt = pricesPerPeriod.startAt.value,
                    endAt = pricesPerPeriod.endAt.value,
                    totalPrice = pricesPerPeriod.totalPrice,
                    totalDiscountPrice = pricesPerPeriod.totalDiscountPrice,
                    totalPriceWithDiscount = pricesPerPeriod.totalPriceWithDiscount,
                    items = pricesPerPeriod.items.map {
                        CustomerOrderRequestPricesPerPeriod.Item(
                            compositionId = it.compositionId.value,
                            itemId = it.itemId.value,
                            price = it.price,
                            discountPrice = it.discountPrice,
                            priceWithDiscount = it.priceWithDiscount
                        )
                    }
                )
        }

        data class CustomerOrderRequestPayment(
            var methods: List<PaymentMethodRequest> = emptyList(),
            val description: String? = null,
            val async: Boolean = false
        )

        data class PaymentMethodRequest(
            val method: String,
            val methodId: String?,
            val price: Price? = null,
            val customFields: JsonNode? = null,
            val securityCode: String? = null,
            val installments: Int? = null
        )

        data class CustomerOrderRequestFreight(
            val address: Address,
            val price: Price,
            val type: String,
            val deliveryTotalTime: Int
        ) {
            data class Address(
                val city: String,
                val complement: String? = null,
                val country: String,
                val district: String,
                val name: String,
                val state: String,
                val street: String,
                val zipCode: String,
                val number: String
            )
        }

        data class Mgm(val invite: String?)

        data class CustomerOrderRequestProduct(
            val productId: String? = null,
            val productTypeId: String,
            val productTypeName: String,
            val installationAttributes: Map<String, Any>
        )

        data class CustomerOrderRequestOffer(
            val catalogOfferId: String,
            val catalogOfferType: String,
            val catalogOfferName: String,
            val catalogOfferDescription: String,
            val offerItems: List<CustomerOrderRequestOfferItem>,
            val price: Price,
            val validity: OfferValidity,
            val pricesPerPeriod: List<CustomerOrderRequestPricesPerPeriod>,
            val quantity: Int
        )

        data class CustomerOrderRequestOfferItem(
            val catalogOfferItemId: String,
            val productTypeId: String,
            val productId: String?,
            val productTypeName: String?,
            val price: Price,
            val recurrent: Boolean,
            val compositionId: String,
            val compositionName: String,
            val userParameters: Map<String, Any>? = null
        )

        data class CouponCode(val code: String, val discounts: List<CouponDiscount>? = listOf())

        data class CouponDiscount(val description: String? = null, val price: Price)

        data class CustomerOrderRequestPricesPerPeriod(
            val totalPrice: Price,
            val totalDiscountPrice: Price,
            val totalPriceWithDiscount: Price,
            val startAt: Int,
            val endAt: Int,
            val items: List<Item>
        ) {
            data class Item(
                val compositionId: String,
                val itemId: String,
                val price: Price,
                val discountPrice: Price,
                val priceWithDiscount: Price
            )
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CustomerOrderResponse(
        @field:[NotBlank] val id: String?,
        val externalId: String?,
        val status: String?,
        val steps: List<Step>?,
        val boleto: Boleto?
    ) {
        init {
            Validator.validate(this)
        }

        fun toCustomerOrder(): CustomerOrder {
            return CustomerOrder(customerOrderId = id!!, status = status, steps = steps, boleto = boleto)
        }

    }

}

class CustomerOrderRequestBuildException(
    val errorCode: ErrorCode,
    val offerId: String
) : RuntimeException()

object RealwaveCatalogManagerAdapter {

    const val CUSTOM_PLAN_TYPE = "CUSTOMPLAN"
    const val CUSTOM_PLAN_OFFER_ID = "custom-plan"

    fun adapt(
        customerOrderRequest: CustomerOrderManagerApiService.CustomerOrderRequest
    ): CustomerOrderManagerApiService.CustomerOrderRequest {
        val customPlanOffers = customerOrderRequest.offers.filter { it.catalogOfferType == CUSTOM_PLAN_TYPE }
        if (customPlanOffers.isNotEmpty()) {
            val notCustomPlanOffers =
                customerOrderRequest.offers.filterNot { it.catalogOfferType == CUSTOM_PLAN_TYPE }
            val customPlanOffer: CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOffer =
                mergeCustomPlanOffers(
                    customPlanOffers
                )
            return customerOrderRequest.copy(
                offers = notCustomPlanOffers + listOf(customPlanOffer)
            )
        }

        return customerOrderRequest
    }

    private fun mergeCustomPlanOffers(
        customPlanOffers: List<CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOffer>
    ): CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOffer {
        val offerItems = buildOfferItems(customPlanOffers)
        return CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOffer(
            catalogOfferId = CUSTOM_PLAN_OFFER_ID,
            catalogOfferType = CUSTOM_PLAN_TYPE,
            catalogOfferName = CUSTOM_PLAN_OFFER_ID,
            validity = customPlanOffers.first().validity,
            offerItems = offerItems,
            price = calculatePrice(offerItems),
            catalogOfferDescription = customPlanOffers.joinToString(" + ") {
                it.catalogOfferDescription
            },
            pricesPerPeriod = listOf(),
            quantity = 1
        )
    }

    private fun buildOfferItems(
        customPlanOffers: List<CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOffer>
    ): List<CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOfferItem> =
        customPlanOffers.flatMap { it.offerItems }

    private fun calculatePrice(
        offerItems: List<CustomerOrderManagerApiService.CustomerOrderRequest.CustomerOrderRequestOfferItem>
    ): Price {
        val price = offerItems.first().price.copy(amount = 0)
        return offerItems.fold(price) { p, customerOrderRequestOffer ->
            p.copy(amount = p.amount + customerOrderRequestOffer.price.amount)
        }
    }

}
