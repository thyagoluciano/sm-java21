package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcFreightRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class FreightMapper : RowMapper<Freight> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Freight {
        val type = rs.getString(JdbcFreightRepository.TYPE)
        val deliveryTotalTime = rs.getInt(JdbcFreightRepository.DELIVERY_TOTAL_TIME)
        val address = rs.getString(JdbcFreightRepository.ADDRESS)
        val priceCurrency = rs.getString(JdbcFreightRepository.PRICE_CURRENCY_COLUMN)
        val priceAmount = rs.getInt(JdbcFreightRepository.PRICE_AMOUNT_COLUMN)
        val priceScale = rs.getInt(JdbcFreightRepository.PRICE_SCALE_COLUMN)

        return Freight(
            type = Freight.Type(type),
            address = address.jsonToObject(Freight.Address::class.java),
            price = Price(
                currency = priceCurrency,
                amount = priceAmount,
                scale = priceScale
            ),
            deliveryTotalTime = Freight.DeliveryTotalTime(deliveryTotalTime)
        )
    }

}
