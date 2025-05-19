package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderItemRepository
import br.com.zup.realwave.sales.manager.query.repository.extractor.PurchaseOrderItemMapper
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Created by luiz on 05/06/17
 */
@Repository
open class JdbcPurchaseOrderItemRepository(val jdbcTemplate: JdbcTemplate) : PurchaseOrderItemRepository {

    companion object {
        const val TABLE_NAME = "ORDER_ITEM"
        const val PURCHASE_ORDER_ID_COLUMN = "purchase_order_id"
        const val ID_COLUMN = "id"
        const val CATALOG_TYPE_COLUMN = "catalog_offer_type"
        const val PRICE_CURRENCY_COLUMN = "price_currency"
        const val PRICE_AMOUNT_COLUMN = "price_amount"
        const val PRICE_SCALE_COLUMN = "price_scale"
        const val VALIDITY_PERIOD_COLUMN = "validity_period"
        const val VALIDITY_DURATION_COLUMN = "validity_duration"
        const val VALIDITY_UNLIMITED_COLUMN = "validity_unlimited"
        const val OFFER_FIELDS_COLUMN = "offer_fields"
        const val CUSTOM_FIELDS_COLUMN = "custom_fields"
        const val CREATED_COLUMN = "created"
        const val UPDATED_COLUMN = "updated"
        const val CATALOG_OFFER_ID_COLUMN = "catalog_offer_id"
        const val OFFER_ITEMS_COLUMN = "offer_items"
        const val PRICES_PER_PERIOD = "prices_per_period"
        const val QUANTITY = "quantity"
    }

    override fun addItem(purchaseOrderId: AggregateId, item: Item): Int {
        val sql = """
               insert into $TABLE_NAME
                           ($PURCHASE_ORDER_ID_COLUMN,
                            $ID_COLUMN,
                            $CATALOG_OFFER_ID_COLUMN,
                            $CATALOG_TYPE_COLUMN,
                            $PRICE_CURRENCY_COLUMN,
                            $PRICE_AMOUNT_COLUMN,
                            $PRICE_SCALE_COLUMN,
                            $VALIDITY_PERIOD_COLUMN,
                            $VALIDITY_DURATION_COLUMN,
                            $VALIDITY_UNLIMITED_COLUMN,
                            $OFFER_FIELDS_COLUMN,
                            $CUSTOM_FIELDS_COLUMN,
                            $OFFER_ITEMS_COLUMN,
                            $PRICES_PER_PERIOD,
                            $QUANTITY,
                            $CREATED_COLUMN)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::JSON, ?::JSON, ?::JSON, ?::JSON, ?, now())
            """
        return jdbcTemplate.update(
            sql,
            purchaseOrderId.value,
            item.id.value,
            item.catalogOfferId.value,
            item.catalogOfferType.value,
            item.price.currency,
            item.price.amount,
            item.price.scale,
            item.validity.period,
            item.validity.duration,
            item.validity.unlimited,
            item.offerFieldsToJson(),
            item.customFieldsToJson(),
            item.offerItemsToJson(),
            item.pricesPerPeriodToJson(),
            item.quantity.value
        )
    }

    override fun updateItem(purchaseOrderId: AggregateId, item: Item): Int {
        val sql = """
               update $TABLE_NAME
                  set $CATALOG_OFFER_ID_COLUMN = ?,
                      $CATALOG_TYPE_COLUMN = ?,
                      $OFFER_FIELDS_COLUMN = ?::JSON,
                      $CUSTOM_FIELDS_COLUMN = ?::JSON,
                      $OFFER_ITEMS_COLUMN = ?::JSON,
                      $UPDATED_COLUMN = now(),
                      $PRICE_CURRENCY_COLUMN = ?,
                      $PRICE_AMOUNT_COLUMN = ?,
                      $PRICE_SCALE_COLUMN = ?,
                      $VALIDITY_PERIOD_COLUMN = ?,
                      $VALIDITY_DURATION_COLUMN = ?,
                      $VALIDITY_UNLIMITED_COLUMN = ?,
                      $PRICES_PER_PERIOD = ?::JSON,
                      $QUANTITY = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                  and $ID_COLUMN = ?
            """
        return jdbcTemplate.update(
            sql,
            item.catalogOfferId.value,
            item.catalogOfferType.value,
            item.offerFieldsToJson(),
            item.customFieldsToJson(),
            item.offerItemsToJson(),
            item.price.currency,
            item.price.amount,
            item.price.scale,
            item.validity.period,
            item.validity.duration,
            item.validity.unlimited,
            item.pricesPerPeriodToJson(),
            item.quantity.value,
            purchaseOrderId.value,
            item.id.value
        )
    }

    override fun findOne(purchaseOrderId: PurchaseOrderId, itemId: Item.Id): Item? {
        val sql = """
            select *
              from $TABLE_NAME
             where $PURCHASE_ORDER_ID_COLUMN = ?
               and $ID_COLUMN = ?
        """

        try {
            return jdbcTemplate.queryForObject(sql, PurchaseOrderItemMapper(), purchaseOrderId.value, itemId.value)
        } catch (e: EmptyResultDataAccessException) {
            return null
        }
    }

    override fun findByPurchaseOrderId(purchaseOrderId: PurchaseOrderId): List<Item> {
        val sql = """
            select *
              from $TABLE_NAME
             where $PURCHASE_ORDER_ID_COLUMN = ?
        """

        return jdbcTemplate.query(sql, PurchaseOrderItemMapper(), purchaseOrderId.value)
    }

    override fun removeItem(purchaseOrderId: AggregateId, itemId: Item.Id): Int {
        val sql = """
            delete from $TABLE_NAME
             where $PURCHASE_ORDER_ID_COLUMN = ?
               and $ID_COLUMN = ?
        """

        return jdbcTemplate.update(sql, purchaseOrderId.value, itemId.value)
    }

}
