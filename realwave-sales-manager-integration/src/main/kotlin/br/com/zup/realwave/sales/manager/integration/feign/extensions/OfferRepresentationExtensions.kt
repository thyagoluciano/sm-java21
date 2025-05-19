package br.com.zup.realwave.sales.manager.integration.feign.extensions

import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferComponentRepresentation
import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferCompositionRepresentation
import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferCustomPlanCompositionRepresentation
import br.com.zup.realwave.cms.commonsmodel.representation.offer.OfferRepresentation
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.common.exception.handler.to.ResourceValue
import br.com.zup.realwave.pcm.commonsmodel.domain.core.Name
import br.com.zup.realwave.pcm.commonsmodel.domain.producttype.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderRequestBuildException
import java.time.LocalDateTime
import java.util.*

fun OfferRepresentation.getProductTypeList(): List<Pair<ProductTypeId, Name>> {
    return getProductTypeIdListFromPlans() +
            getProductTypeIdListFromCustomPlan() +
            getProductTypeIdListFromAddOn() +
            getProductTypeIdListFromSales()
}

fun OfferRepresentation.getProductTypeIdListFromPlans(): List<Pair<ProductTypeId, Name>> {
    val itemsPlusBonusesProductIds = this.plans.map {
        val items =
            it.items.map { Pair(it.composition.component.productType.id, it.composition.component.productType.name) }
        val bonuses =
            it.bonuses?.firstOrNull { it.availability.isAvailable(LocalDateTime.now()) }?.items?.map {
                Pair(it.component.productType.id, it.component.productType.name)
            } ?: listOf()

        items.plus(bonuses)
    }

    return itemsPlusBonusesProductIds.flatten()
}

fun OfferRepresentation.getProductTypeIdListFromCustomPlan(): List<Pair<ProductTypeId, Name>> {
    return this.customPlanConfigurations.map {
        Pair(it.component.productType.id, it.component.productType.name)
    } + getProductTypeIdListFromCustomPlanBonus()
}

fun OfferRepresentation.getProductTypeIdListFromCustomPlanBonus(): List<Pair<ProductTypeId, Name>> {
    val productTypeIds = this.customPlanConfigurations.map {
        it.items.map {
            it.bonuses?.firstOrNull { it.availability.isAvailable(LocalDateTime.now()) }?.items?.map {
                Pair(
                    it.component.productType.id,
                    it.component.productType.name
                )
            } ?: listOf()
        }.flatten()
    }

    return if (!productTypeIds.isEmpty()) productTypeIds.reduce { acc, list -> acc + list } else listOf()
}

fun OfferRepresentation.getProductTypeIdListFromAddOn(): List<Pair<ProductTypeId, Name>> {
    val productTypeIds = this.addons.map {
        it.items.map {
            Pair(
                it.composition.component.productType.id,
                it.composition.component.productType.name
            )
        }
    }
    return if (!productTypeIds.isEmpty()) productTypeIds.reduce { acc, list -> acc + list } else listOf()
}

fun OfferRepresentation.getProductTypeIdListFromSales(): List<Pair<ProductTypeId, Name>> {
    val productTypeIds = this.sales.map {
        it.items.map {
            Pair(
                it.composition.component.productType.id,
                it.composition.component.productType.name
            )
        }
    }
    return if (!productTypeIds.isEmpty()) productTypeIds.reduce { acc, list -> acc + list } else listOf()
}

fun OfferRepresentation.getOfferCompositionRepresentation(
    id: String,
    type: CatalogOfferType
): OfferCompositionRepresentation? {
    try {
        return when (type.value.toLowerCase()) {
            "plan" -> this.getOfferCompositionRepresentationFromPlan(id)
            "customplan" -> this.getOfferCompositionRepresentationFromCustomPlan(id)
            "addon" -> this.getOfferCompositionRepresentationAddon(id)
            "sale" -> this.getOfferCompositionRepresentationFromSales(id)
            else -> throw NotFoundException(ResourceValue(CatalogOfferType::class.java, type.value))
        }
    } catch (e: NoSuchElementException) {
        throw CustomerOrderRequestBuildException(
            errorCode = PurchaseOrderErrorCode.CATALOG_OFFER_NOT_RETURNED_BY_OFFER_ITEM_DETAILS,
            offerId = id
        )
    }
}

fun OfferRepresentation.getOfferCompositionRepresentationFromPlan(id: String): OfferCompositionRepresentation? {
    return this.plans.map { it.items.first { it.id.id == id } }.first().composition
} //TODO add bonus

data class ComponentStructure(
    val offerComponentRepresentation: OfferComponentRepresentation,
    val offerCustomPlanCompositionRepresentation: OfferCustomPlanCompositionRepresentation
)

fun OfferRepresentation.getOfferCompositionRepresentationFromCustomPlan(id: String): OfferCompositionRepresentation? {
    return this.from(
        this.customPlanConfigurations.map {
            ComponentStructure(
                offerComponentRepresentation = it.component,
                offerCustomPlanCompositionRepresentation =
                this.customPlanConfigurations
                    .flatMap { it.items }
                    .first { it.id.id == id }
                    .composition

            )
        }.first()
    )
    //TODO add bonus
}

fun OfferRepresentation.getOfferCompositionRepresentationAddon(id: String): OfferCompositionRepresentation {
    return this.addons.map { it.items.first { it.id.id == id } }.first().composition
}

fun OfferRepresentation.getOfferCompositionRepresentationFromSales(id: String): OfferCompositionRepresentation {
    return this.sales.map { it.items.first { it.id.id == id } }.first().composition
}

fun OfferRepresentation.from(componentStrucutre: ComponentStructure): OfferCompositionRepresentation {
    return OfferCompositionRepresentation(
        id = componentStrucutre.offerCustomPlanCompositionRepresentation.id,
        name = componentStrucutre.offerCustomPlanCompositionRepresentation.name,
        attributes = componentStrucutre.offerCustomPlanCompositionRepresentation
            .attributes,
        component = componentStrucutre.offerComponentRepresentation,
        systemParameters = componentStrucutre.offerComponentRepresentation.systemParameters
    )
}

fun OfferRepresentation.getOfferNameDescription(
    id: String,
    type: CatalogOfferType,
    offerItems: List<Item.OfferItem>
): Pair<String, String?> {
    try {
        return when (type.value.toLowerCase()) {
            "plan" -> this.plans.first { it.id.id == id }.let { Pair(it.name.name, it.description?.description) }
            "addon" -> this.addons.first { it.id.id == id }.let { Pair(it.name.name, it.description?.description) }
            "customplan" -> {
                val customPlan = this.customPlanConfigurations.first { it.id.id == id }
                Pair(offerItems.joinToString(" + ") { offerItem ->
                    customPlan.items.first { item ->
                        item.id.id == offerItem.catalogOfferItemId.value
                    }.composition.name.name
                }, null)
            }
            else -> Pair("NOT FOUND", null)
        }
    } catch (e: NoSuchElementException) {
        throw CustomerOrderRequestBuildException(
            errorCode = PurchaseOrderErrorCode.CATALOG_OFFER_NOT_RETURNED_BY_OFFER_DETAILS,
            offerId = id
        )
    }
}
