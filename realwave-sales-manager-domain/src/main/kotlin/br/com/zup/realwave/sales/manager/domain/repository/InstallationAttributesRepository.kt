package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId

/**
 * Created by marcosgm on 05/06/17
 */
interface InstallationAttributesRepository {

    fun findOne(purchaseOrderId: String, productTypeId: String): InstallationAttribute

    fun update(purchaseOrderId: String, installationAttribute: InstallationAttribute): Int

    fun findAll(purchaseOrderId: String): Map<ProductTypeId, InstallationAttribute>

    fun delete(purchaseOrderId: String, productTypeId: ProductTypeId): Int

}
