package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.query.repository.JdbcDiscountRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class CouponMapper : RowMapper<Pair<String, CouponCode.Discount>> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Pair<String, CouponCode.Discount> {
        val couponCode = rs.getString(JdbcDiscountRepository.COUPON_CODE)
        val type = rs.getString(JdbcDiscountRepository.TYPE)
        val segmentId = rs.getString(JdbcDiscountRepository.SEGMENT_ID)
        val segmentName = rs.getString(JdbcDiscountRepository.SEGMENT_NAME)
        val segmentType = rs.getString(JdbcDiscountRepository.SEGMENT_TYPE)
        val priceCurrency = rs.getString(JdbcDiscountRepository.PRICE_CURRENCY_COLUMN)
        val priceValue = rs.getInt(JdbcDiscountRepository.PRICE_AMOUNT_COLUMN)
        val priceScale = rs.getInt(JdbcDiscountRepository.PRICE_SCALE_COLUMN)
        val discountPercentage = rs.getInt(JdbcDiscountRepository.DISCOUNT_PERCENTAGE)

        var price: Price? = null
        if (null != priceCurrency && priceCurrency != "") {
            price = Price(
                currency = priceCurrency,
                amount = priceValue,
                scale = priceScale
            )
        }

        return Pair(
            type,
            CouponCode.Discount(
                segment = segmentId?.let {
                    CouponCode.Segment(
                        id = segmentId,
                        name = segmentName,
                        type = segmentType
                    )
                },
                discount = price,
                discountAsPercent = discountPercentage
            )
        )
    }

}
