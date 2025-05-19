package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.infrastructure.jsonToListObject
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcPurchaseOrderItemRepository
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Created by marcosgm on 05/06/17
 */
class PurchaseOrderItemMapper : RowMapper<Item> {

    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call `next()` on
     * the ResultSet; it is only supposed to map values of the current row.
     * @param rs the ResultSet to map (pre-initialized for the current row)
     * *
     * @param rowNum the number of the current row
     * *
     * @return the result object for the current row
     * *
     * @throws SQLException if a SQLException is encountered getting
     * * column values (that is, there's no need to catch SQLException)
     */
    override fun mapRow(rs: ResultSet, rowNum: Int): Item {
        val itemId = rs.getString(JdbcPurchaseOrderItemRepository.ID_COLUMN)
        val offerId = rs.getString(JdbcPurchaseOrderItemRepository.CATALOG_OFFER_ID_COLUMN)
        val type = rs.getString(JdbcPurchaseOrderItemRepository.CATALOG_TYPE_COLUMN)
        val priceCurrency = rs.getString(JdbcPurchaseOrderItemRepository.PRICE_CURRENCY_COLUMN)
        val priceValue = rs.getInt(JdbcPurchaseOrderItemRepository.PRICE_AMOUNT_COLUMN)
        val priceScale = rs.getInt(JdbcPurchaseOrderItemRepository.PRICE_SCALE_COLUMN)
        val validityPeriod = rs.getString(JdbcPurchaseOrderItemRepository.VALIDITY_PERIOD_COLUMN)
        val validityDuration = rs.getInt(JdbcPurchaseOrderItemRepository.VALIDITY_DURATION_COLUMN)
        val validityUnlimited = rs.getBoolean(JdbcPurchaseOrderItemRepository.VALIDITY_UNLIMITED_COLUMN)
        val offerFields = rs.getString(JdbcPurchaseOrderItemRepository.OFFER_FIELDS_COLUMN)
        val customFields = rs.getString(JdbcPurchaseOrderItemRepository.CUSTOM_FIELDS_COLUMN)
        val offersItems = rs.getString(JdbcPurchaseOrderItemRepository.OFFER_ITEMS_COLUMN)
        val pricesPerPeriod = rs.getString(JdbcPurchaseOrderItemRepository.PRICES_PER_PERIOD)
        val quantity = rs.getInt(JdbcPurchaseOrderItemRepository.QUANTITY)

        return Item(
            id = Item.Id(itemId),
            catalogOfferId = CatalogOfferId(offerId),
            catalogOfferType = CatalogOfferType(type),
            price = Price(
                currency = priceCurrency,
                amount = priceValue,
                scale = priceScale
            ),
            validity = OfferValidity(
                period = validityPeriod,
                duration = validityDuration,
                unlimited = validityUnlimited

            ),
            offerFields = OfferFields(offerFields.jsonToObject(JsonNode::class.java)),
            customFields = CustomFields(customFields.jsonToObject(JsonNode::class.java)),
            offerItems = offersItems.jsonToListObject(Item.OfferItem::class.java),
            pricesPerPeriod = pricesPerPeriod.jsonToListObject(PricePerPeriod::class.java),
            quantity = Item.Quantity(value = quantity)
        )
    }

}
