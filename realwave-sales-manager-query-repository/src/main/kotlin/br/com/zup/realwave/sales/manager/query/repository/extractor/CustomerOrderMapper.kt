package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.CustomerOrder
import br.com.zup.realwave.sales.manager.domain.Step
import br.com.zup.realwave.sales.manager.infrastructure.jsonToListObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcCustomerOrderRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

/**
 * Created by branquinho on 12/07/17.
 */
@Component
class CustomerOrderMapper : RowMapper<CustomerOrder> {

    override fun mapRow(rs: ResultSet, rowNum: Int): CustomerOrder {
        val customerOrderId = rs.getString(JdbcCustomerOrderRepository.CUSTOMER_ORDER_ID_COLUMN)
        val status = rs.getString(JdbcCustomerOrderRepository.STATUS_COLUMN)
        val steps = rs.getString(JdbcCustomerOrderRepository.STEPS_COLUMN).jsonToListObject(Step::class.java)

        val customerOrder = CustomerOrder(
            customerOrderId = customerOrderId,
            status = status,
            steps = steps
        )

        return customerOrder
    }

}
