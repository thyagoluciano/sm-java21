package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import com.fasterxml.jackson.databind.JsonNode

/**
 * Created by marcosgm on 02/06/17.
 */
data class CouponCode(
    val code: String,
    var customFields: JsonNode? = null,
    val description: String? = null,
    val reward: Reward? = null
) {

    fun customFieldsToStringJson() = customFields?.objectToJson()

    data class Reward(val type: String, val discounts: List<Discount>)

    enum class RewardType(name: String) {
        DISCOUNT_PERCENT("DISCOUNT_PERCENT"),
        DISCOUNT_MONEY("DISCOUNT_MONEY")
    }

    data class Discount(val segment: Segment? = null, val discount: Price? = null, val discountAsPercent: Int? = null)

    data class Segment(val id: String, val name: String, val type: String)

}
