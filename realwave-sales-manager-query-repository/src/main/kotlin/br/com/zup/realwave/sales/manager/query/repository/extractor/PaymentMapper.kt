package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcPaymentRepository
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class PaymentMapper : RowMapper<Payment.PaymentMethod> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Payment.PaymentMethod {
        val paymentMethod = rs.getString(JdbcPaymentRepository.PAYMENT_METHOD_COLUMN)
        val paymentMethodId = rs.getString(JdbcPaymentRepository.PAYMENT_METHOD_ID_COLUMN)
        val priceCurrency = rs.getString(JdbcPaymentRepository.PRICE_CURRENCY_COLUMN)
        val priceValue = rs.getInt(JdbcPaymentRepository.PRICE_AMOUNT_COLUMN)
        val priceScale = rs.getInt(JdbcPaymentRepository.PRICE_SCALE_COLUMN)
        val customFields = rs.getString(JdbcPaymentRepository.CUSTOM_FIELDS)
        val securityCodeInformed = rs.getBoolean(JdbcPaymentRepository.SECURITY_CODE_INFORMED)
        val installments = rs.getInt(JdbcPaymentRepository.INSTALLMENTS)

        var price: Price? = null
        if (null != priceCurrency && priceCurrency != "") {
            price = Price(
                currency = priceCurrency,
                amount = priceValue,
                scale = priceScale
            )
        }
        val payment = Payment.PaymentMethod(
            method = paymentMethod,
            methodId = paymentMethodId,
            price = price,
            customFields = customFields?.jsonToObject(JsonNode::class.java),
            securityCodeInformed = securityCodeInformed,
            installments = installments
        )

        return payment
    }

}
