package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.repository.CustomerOrderRepository
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.query.repository.extractor.CustomerOrderMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Created by branquinho on 11/07/17.
 */
@Repository
open class JdbcCustomerOrderRepository : CustomerOrderRepository {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var customerOrderMapper: CustomerOrderMapper

    companion object {
        const val TABLE_NAME: String = "customer_order"
        const val ID_COLUMN: String = "id"
        const val CUSTOMER_ORDER_ID_COLUMN: String = "customer_order_id"
        const val STATUS_COLUMN: String = "status"
        const val STEPS_COLUMN: String = "steps"
    }

    override fun findOne(purchaseOrderId: AggregateId): CustomerOrder? {
        val sql = "select * from $TABLE_NAME where $ID_COLUMN = ?"
        val customerOrder = jdbcTemplate.query(sql, customerOrderMapper, purchaseOrderId.value)

        return when (customerOrder.size) {
            0 -> null
            1 -> customerOrder[0]
            else -> throw IncorrectResultSizeDataAccessException(1, customerOrder.size)
        }
    }

    override fun saveCustomerOrder(purchaseOrderId: AggregateId, customerOrder: CustomerOrder): Int {
        val sql = """
                    INSERT INTO $TABLE_NAME ($ID_COLUMN,$CUSTOMER_ORDER_ID_COLUMN,$STATUS_COLUMN,$STEPS_COLUMN)
                    VALUES (?, ?, ?, ?::JSON)
                    ON CONFLICT ($ID_COLUMN)
                    DO UPDATE SET $CUSTOMER_ORDER_ID_COLUMN = ?, $STATUS_COLUMN = ?, $STEPS_COLUMN = ?::JSON
                  """

        val stepsJson = customerOrder.steps.objectToJson()

        return jdbcTemplate.update(
            sql, purchaseOrderId.value, customerOrder.customerOrderId, customerOrder.status, stepsJson,
            customerOrder.customerOrderId, customerOrder.status, stepsJson
        )
    }

}
