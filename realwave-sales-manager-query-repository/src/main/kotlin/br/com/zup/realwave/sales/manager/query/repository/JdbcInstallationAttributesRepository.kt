package br.com.zup.realwave.sales.manager.query.repository

import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.repository.InstallationAttributesRepository
import br.com.zup.realwave.sales.manager.query.repository.extractor.InstallationAttributesMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Created by marcosgm on 05/06/17.
 */
@Repository
open class JdbcInstallationAttributesRepository : InstallationAttributesRepository {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        const val TABLE_NAME: String = "INSTALLATION_ATTRIBUTES"
        const val PURCHASE_ORDER_ID_COLUMN: String = "purchase_order_id"
        const val PRODUCT_TYPE_ID_COLUMN: String = "product_type_id"
        const val ATTRIBUTES_COLUMN: String = "attributes"
        const val CREATED_COLUMN: String = "created"
        const val UPDATED_COLUMN: String = "updated"
    }

    override fun findOne(purchaseOrderId: String, productTypeId: String): InstallationAttribute {
        val sql = "select * from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ? and $PRODUCT_TYPE_ID_COLUMN =  ?"
        return jdbcTemplate.queryForObject(sql, InstallationAttributesMapper(), purchaseOrderId, productTypeId)
    }

    override fun findAll(purchaseOrderId: String): Map<ProductTypeId, InstallationAttribute> {
        val installationAttributes = HashMap<ProductTypeId, InstallationAttribute>()
        val sql = "select * from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ?"
        val list = jdbcTemplate.query(sql, InstallationAttributesMapper(), purchaseOrderId)
        list.forEach { installation -> installationAttributes[installation.productTypeId] = installation }
        return installationAttributes
    }

    override fun update(purchaseOrderId: String, installationAttribute: InstallationAttribute): Int {
        var rows = tryToUpdate(purchaseOrderId, installationAttribute)
        if (rows == 0) {
            val sql = "insert into $TABLE_NAME ($PURCHASE_ORDER_ID_COLUMN," +
                    "$PRODUCT_TYPE_ID_COLUMN, $ATTRIBUTES_COLUMN, $CREATED_COLUMN) values (?, ?, ?::JSON, now())"
            rows = jdbcTemplate.update(
                sql, purchaseOrderId, installationAttribute.productTypeId.toString(),
                installationAttribute.attributesToJson()
            )
        }
        return rows
    }

    override fun delete(purchaseOrderId: String, productTypeId: ProductTypeId): Int {
        val sql = "delete from $TABLE_NAME " +
                "where $PURCHASE_ORDER_ID_COLUMN = ? " +
                "and $PRODUCT_TYPE_ID_COLUMN = ?"
        return jdbcTemplate.update(sql, purchaseOrderId, productTypeId.toString())
    }

    private fun tryToUpdate(purchaseOrderId: String, installationAttribute: InstallationAttribute): Int {
        val sql = """
                update $TABLE_NAME set $ATTRIBUTES_COLUMN = ?::JSON, $UPDATED_COLUMN = now()
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $PRODUCT_TYPE_ID_COLUMN = ?
        """
        return jdbcTemplate.update(
            sql, installationAttribute.attributesToJson(),
            purchaseOrderId, installationAttribute.productTypeId.toString()
        )
    }

}
