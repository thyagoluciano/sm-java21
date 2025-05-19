package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.repository.DiscountRepository
import br.com.zup.realwave.sales.manager.query.repository.extractor.CouponMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class JdbcDiscountRepository : DiscountRepository {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        const val TABLE_NAME: String = "DISCOUNTS"
        const val COUPON_CODE: String = "coupon_code"
        const val TYPE: String = "type"
        const val SEGMENT_ID: String = "segment_id"
        const val SEGMENT_NAME: String = "segment_name"
        const val SEGMENT_TYPE: String = "segment_type"
        const val PRICE_CURRENCY_COLUMN = "price_currency"
        const val PRICE_AMOUNT_COLUMN = "price_amount"
        const val PRICE_SCALE_COLUMN = "price_scale"
        const val DISCOUNT_PERCENTAGE = "discount_percentage"
        const val CREATED_COLUMN: String = "created"
    }

    override fun saveDiscount(coupon: CouponCode): Int {
        val sql = """
                   INSERT INTO $TABLE_NAME
                               ($COUPON_CODE,
                               $TYPE,
                               $SEGMENT_ID,
                               $SEGMENT_NAME,
                               $SEGMENT_TYPE,
                               $PRICE_CURRENCY_COLUMN,
                               $PRICE_AMOUNT_COLUMN,
                               $PRICE_SCALE_COLUMN,
                               $DISCOUNT_PERCENTAGE,
                               $CREATED_COLUMN
                                )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                """

        val batchArgs = coupon.reward!!.discounts.mapIndexed { index, discounts ->
            arrayOf(
                coupon.code,
                coupon.reward!!.type,
                discounts.segment?.id,
                discounts.segment?.name,
                discounts.segment?.type,
                discounts.discount?.currency,
                discounts.discount?.amount,
                discounts.discount?.scale,
                discounts.discountAsPercent
            )
        }

        val response = jdbcTemplate.batchUpdate(sql, batchArgs)
        return if (null != response.firstOrNull { it != 1 }) response.first { it != 1 } else 1
    }

    override fun findByCoupon(coupon: CouponCode): CouponCode.Reward? {
        val discountList: MutableList<CouponCode.Discount> = mutableListOf()
        val sql = "select * from $TABLE_NAME where $COUPON_CODE = ?"
        val list = jdbcTemplate.query(sql, CouponMapper(), coupon.code)
        return if (list.isEmpty()) {
            null
        } else {
            list.forEach { it ->
                discountList.add(it.second)
            }
            return CouponCode.Reward(
                type = list.first().first,
                discounts = discountList
            )
        }
    }

}
