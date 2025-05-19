package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.eventsourcing.core.config.objectToJson
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.repository.PaymentRepository
import br.com.zup.realwave.sales.manager.query.repository.extractor.PaymentMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class JdbcPaymentRepository : PaymentRepository {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        const val TABLE_NAME: String = "PAYMENT"
        const val PURCHASE_ORDER_ID_COLUMN: String = "purchase_order_id"
        const val PAYMENT_METHOD_COLUMN = "payment_method"
        const val PAYMENT_METHOD_ID_COLUMN = "payment_method_id"
        const val PRICE_CURRENCY_COLUMN = "price_currency"
        const val PRICE_AMOUNT_COLUMN = "price_amount"
        const val PRICE_SCALE_COLUMN = "price_scale"
        const val PAYMENT_ORDER = "payment_order"
        const val CUSTOM_FIELDS = "custom_fields"
        const val CREATED_COLUMN: String = "created"
        const val SECURITY_CODE_INFORMED = "security_code_informed"
        const val INSTALLMENTS = "installments"
    }

    override fun savePayments(purchaseOrderId: AggregateId, paymentList: Payment): Int {

        deleteAll(purchaseOrderId)

        return saveNewPayments(paymentList.methods, purchaseOrderId)
    }

    override fun updatePaymentSecurityCode(
        purchaseOrderId: AggregateId,
        securityCodeInformed: List<SecurityCodeInformed>?
    ) {
        securityCodeInformed?.forEach {

            val sql = """update $TABLE_NAME
                            set $SECURITY_CODE_INFORMED = ?
                          where $PURCHASE_ORDER_ID_COLUMN = ?
                            and $PAYMENT_METHOD_ID_COLUMN = ?"""

            jdbcTemplate.update(sql, it.securityCodeInformed, purchaseOrderId.value, it.methodId)
        }
    }

    private fun deleteAll(purchaseOrderId: AggregateId) {
        val sql = """
                   DELETE FROM $TABLE_NAME
                   WHERE $PURCHASE_ORDER_ID_COLUMN = ?
                """
        jdbcTemplate.update(sql, purchaseOrderId.value)
    }

    private fun saveNewPayments(paymentList: List<Payment.PaymentMethod>, purchaseOrderId: AggregateId): Int {
        val sql = """
                   INSERT INTO $TABLE_NAME
                               ($PURCHASE_ORDER_ID_COLUMN,
                                $PAYMENT_METHOD_COLUMN,
                               $PAYMENT_METHOD_ID_COLUMN,
                               $PRICE_CURRENCY_COLUMN,
                               $PRICE_AMOUNT_COLUMN,
                               $PRICE_SCALE_COLUMN,
                               $INSTALLMENTS,
                               $CUSTOM_FIELDS,
                               $PAYMENT_ORDER,
                               $CREATED_COLUMN)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?::JSON, ?, now())
                """

        val batchArgs = paymentList.mapIndexed { index, payment ->
            arrayOf(
                purchaseOrderId.value,
                payment.method,
                payment.methodId,
                payment.price?.currency,
                payment.price?.amount,
                payment.price?.scale,
                payment.installments,
                payment.customFields.objectToJson(),
                index
            )
        }
        val response = jdbcTemplate.batchUpdate(sql, batchArgs)
        return if (null != response.firstOrNull { it != 1 }) response.first { it != 1 } else 1
    }

    override fun findAll(purchaseOrderId: AggregateId): List<Payment.PaymentMethod> {
        val paymentList: MutableList<Payment.PaymentMethod> = mutableListOf()
        val sql = "select * from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ? order by $PAYMENT_ORDER"
        val list = jdbcTemplate.query(sql, PaymentMapper(), purchaseOrderId.value)
        list.forEach { it -> paymentList.add(it) }
        return paymentList
    }

    override fun updateMethodId(purchaseOrderId: AggregateId, methodId: String): Int {
        val sql = """UPDATE $TABLE_NAME
                     SET $PAYMENT_METHOD_ID_COLUMN = ?
                     WHERE $PURCHASE_ORDER_ID_COLUMN = ?; """

        return jdbcTemplate.update(sql, methodId, purchaseOrderId.value)
    }
}
