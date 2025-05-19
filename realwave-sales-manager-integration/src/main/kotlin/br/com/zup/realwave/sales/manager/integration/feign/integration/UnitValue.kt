package br.com.zup.realwave.sales.manager.integration.feign.integration

import java.math.BigDecimal

/**
 * Created by luizs on 19/07/2017
 */
data class UnitValue(
    val value: Long,
    val scale: Int,
    val unit: String?,
    val volumeFormatted: String?
) {

    override fun toString(): String {
        return "${BigDecimal.valueOf(value, scale)} $unit formater:$volumeFormatted"
    }
}
