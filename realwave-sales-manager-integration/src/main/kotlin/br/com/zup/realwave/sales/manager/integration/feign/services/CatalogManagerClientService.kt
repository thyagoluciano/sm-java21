package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.cm.commonsmodel.domain.addon.AddonRenewalType
import br.com.zup.realwave.cm.commonsmodel.domain.core.CurrencyType
import br.com.zup.realwave.cm.commonsmodel.domain.core.DiscountPrice
import br.com.zup.realwave.cm.commonsmodel.domain.core.Price
import br.com.zup.realwave.cm.commonsmodel.domain.core.validity.Validity
import br.com.zup.realwave.cm.commonsmodel.domain.core.validity.ValidityPeriod
import br.com.zup.realwave.cm.commonsmodel.domain.offer.OfferId
import br.com.zup.realwave.cm.commonsmodel.domain.offer.OfferItemId
import br.com.zup.realwave.cm.commonsmodel.domain.offer.OfferType
import br.com.zup.realwave.cms.client.offer.CmsOfferClient
import br.com.zup.realwave.cms.client.offer.request.OfferTypeValidateItemRequest
import br.com.zup.realwave.cms.client.offer.request.OfferTypeValidateRequest
import br.com.zup.realwave.cms.client.offer.request.OfferValidateRequest
import br.com.zup.realwave.cms.client.offer.request.PricesPerPeriodItemRequest
import br.com.zup.realwave.cms.client.offer.request.PricesPerPeriodRequest
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.pcm.commonsmodel.domain.composition.CompositionId
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.service.CatalogManagerService
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode.Companion
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CatalogSearchErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class CatalogManagerClientService(
    val catalogManagerApiService: CmsOfferClient
) : CatalogManagerService {

    private val log = LogManager.getLogger(this.javaClass)

    override fun validateOffers(items: MutableSet<Item>) {
        val offerValidateRequest = offerValidateRequest(items)

        log.info("Begin validate catalog on cms api")
        val offerValidateRepresentation = catalogManagerApiService.validate(offerValidateRequest)
        log.info("End validate catalog on cms api")

        if (offerValidateRepresentation.errors.isNotEmpty()) {
            throw CatalogSearchErrorDecoder.CatalogSearchValidationException(
                errorCode = PurchaseOrderErrorCode.CATALOG_SEARCH_OFFER_INVALID,
                response = offerValidateRepresentation
            )
        }
    }

    private fun offerValidateRequest(items: MutableSet<Item>): OfferValidateRequest {
        return OfferValidateRequest(
            items.map { offer ->
                OfferTypeValidateRequest(
                    id = OfferId(offer.catalogOfferId.value),
                    type = offerType(offer),
                    validity = validity(offer)!!,
                    items = offer.offerItems.map { offerItem ->
                        OfferTypeValidateItemRequest(
                            id = OfferItemId(offerItem.catalogOfferItemId.value),
                            price = Price.of(
                                offerItem.price.amount.toLong(),
                                offerItem.price.scale,
                                currencyType(offerItem)
                            ),
                            recurrent = offerItem.recurrent,
                            renewalType = renewalType(offerItem.recurrent)
                        )
                    },
                    pricesPerPeriod = offer.pricesPerPeriod.map {
                        PricesPerPeriodRequest(
                            totalPrice = Price.of(
                                it.totalPrice.amount.toLong(),
                                it.totalPrice.scale,
                                it.totalPrice.currency
                            ),
                            totalDiscountPrice = Price.of(
                                it.totalDiscountPrice.amount.toLong(),
                                it.totalDiscountPrice.scale,
                                it.totalDiscountPrice.currency
                            ),
                            totalPriceWithDiscount = Price.of(
                                it.totalPriceWithDiscount.amount.toLong(),
                                it.totalPriceWithDiscount.scale,
                                it.totalPriceWithDiscount.currency
                            ),
                            items = it.items.map {
                                PricesPerPeriodItemRequest(
                                    compositionId = CompositionId(it.compositionId.value),
                                    itemId = OfferId(it.itemId.value),
                                    price = Price.of(
                                        it.price.amount.toLong(),
                                        it.price.scale,
                                        it.price.currency
                                    ),
                                    discountPrice = DiscountPrice.of(
                                        it.discountPrice.amount.toLong(),
                                        it.discountPrice.scale,
                                        it.discountPrice.currency
                                    ),
                                    priceWithDiscount = Price.of(
                                        it.priceWithDiscount.amount.toLong(),
                                        it.priceWithDiscount.scale,
                                        it.priceWithDiscount.currency
                                    )
                                )
                            },
                            startAt = it.startAt.value,
                            endAt = it.endAt.value
                        )
                    },
                    bonusId = null,
                    reward = null
                )
            }
        )
    }

    private fun validity(offer: Item): Validity? {
        return if (offer.validity.unlimited) {
            Validity.ofUnlimited()
        } else {
            Validity.of(
                validityPeriod(offer),
                offer.validity.duration
            )
        }
    }

    private fun validityPeriod(offer: Item): ValidityPeriod? {
        return try {
            ValidityPeriod.valueOf(offer.validity.period!!)
        } catch (e: Exception) {
            throw BusinessException.of(
                Companion.CATALOG_SEARCH_VALUE_ATTRIBUTE_ERROR,
                offer.validity.period
            )
        }
    }

    private fun renewalType(recurrent: Boolean): AddonRenewalType {
        return if (recurrent) {
            AddonRenewalType.RENEW
        } else {
            AddonRenewalType.NONE
        }
    }

    private fun currencyType(offerItem: Item.OfferItem): CurrencyType {
        return try {
            CurrencyType.valueOf(offerItem.price.currency)
        } catch (e: Exception) {
            throw BusinessException.of(
                Companion.CATALOG_SEARCH_VALUE_ATTRIBUTE_ERROR,
                offerItem.price.currency
            )
        }
    }

    private fun offerType(offer: Item): OfferType {
        return try {
            OfferType.valueOf(offer.catalogOfferType.value)
        } catch (e: Exception) {
            throw BusinessException.of(
                Companion.CATALOG_SEARCH_VALUE_ATTRIBUTE_ERROR,
                offer.catalogOfferType.value
            )
        }
    }

}
