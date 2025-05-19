package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcInstallationAttributesRepository
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Created by marcosgm on 05/06/17
 */
class InstallationAttributesMapper : RowMapper<InstallationAttribute> {

    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call `next()` on
     * the ResultSet; it is only supposed to map values of the current row.
     * @param rs the ResultSet to map (pre-initialized for the current row)
     * *
     * @param rowNum the number of the current row
     * *
     * @return the result object for the current row
     * *
     * @throws SQLException if a SQLException is encountered getting
     * * column values (that is, there's no need to catch SQLException)
     */
    override fun mapRow(rs: ResultSet, rowNum: Int): InstallationAttribute {
        val productTypeId = rs.getString(JdbcInstallationAttributesRepository.PRODUCT_TYPE_ID_COLUMN)
        val attributes = rs.getString(JdbcInstallationAttributesRepository.Companion.ATTRIBUTES_COLUMN)

        val customFieldsNode = attributes.jsonToObject(object : TypeReference<Map<String, Any>>() {})

        return InstallationAttribute(productTypeId = ProductTypeId(productTypeId), attributes = customFieldsNode)
    }

}
