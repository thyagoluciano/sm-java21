package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.repository.FreightRepository
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.query.repository.extractor.FreightMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class JdbcFreightRepository : FreightRepository {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        const val TABLE_NAME: String = "FREIGHT"
        const val PURCHASE_ORDER_ID_COLUMN: String = "purchase_order_id"
        const val CATALOG_ITEM_ID = "catalog_item_id"
        const val ADDRESS = "address"
        const val QUANTITY = "quantity"
        const val TYPE = "type"
        const val DELIVERY_TOTAL_TIME = "delivery_total_time"
        const val PRICE_CURRENCY_COLUMN = "price_currency"
        const val PRICE_AMOUNT_COLUMN = "price_amount"
        const val PRICE_SCALE_COLUMN = "price_scale"
        const val CREATED_COLUMN: String = "created"
    }

    override fun saveFreight(purchaseOrderId: AggregateId, freight: Freight): Int {

        deleteAll(purchaseOrderId)

        val sql = """
           INSERT INTO $TABLE_NAME (
                $PURCHASE_ORDER_ID_COLUMN,
                $ADDRESS,
                $TYPE,
                $DELIVERY_TOTAL_TIME,
                $PRICE_CURRENCY_COLUMN,
                $PRICE_AMOUNT_COLUMN,
                $PRICE_SCALE_COLUMN,
                $CREATED_COLUMN
            ) VALUES (
                ?, ?::JSON, ?, ?, ?, ?, ?, now()
            )
        """

        return jdbcTemplate.update(
            sql,
            purchaseOrderId.value,
            freight.address.objectToJson(),
            freight.type.value,
            freight.deliveryTotalTime.value,
            freight.price.currency,
            freight.price.amount,
            freight.price.scale
        )
    }

    private fun deleteAll(purchaseOrderId: AggregateId) {
        val sql = "delete FROM $TABLE_NAME WHERE $PURCHASE_ORDER_ID_COLUMN = ?"
        jdbcTemplate.update(sql, purchaseOrderId.value)
    }

    override fun find(purchaseOrderId: AggregateId): Freight? {
        val sql = "select * from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ?"
        val freight = jdbcTemplate.query(sql, FreightMapper(), purchaseOrderId.value)
        return when (freight.size) {
            0 -> null
            1 -> freight[0]
            else -> throw IncorrectResultSizeDataAccessException(1, freight.size)
        }
    }
}
