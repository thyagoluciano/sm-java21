package br.com.zup.test.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.deleteInstallationAttributes
import br.com.zup.realwave.sales.manager.domain.installationAttributesFor
import br.com.zup.realwave.sales.manager.domain.updateInstallationAttributes
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PurchaseOrderInstallationAttributesTest {

    @Test
    fun testUpdateInstallationAttributes() {
        val purchaseOrder = buildPurchaseOrder()
        val productTypeId = ProductTypeId("productTypeId")
        val attributes = mapOf(
            "param1" to "value1",
            "param2" to "value2"
        )
        val installationAttribute = InstallationAttribute(productTypeId = productTypeId, attributes = attributes)
        purchaseOrder.updateInstallationAttributes(installationAttribute = installationAttribute)
        assertEquals(expected = installationAttribute, actual = purchaseOrder.installationAttributes[productTypeId])
    }

    @Test(expected = BusinessException::class)
    fun testUpdateInstallationAttributesOfInvalidPurchaseOrderStatus() {
        val purchaseOrder = buildPurchaseOrder(status = PurchaseOrderStatus.COMPLETED)
        purchaseOrder.updateInstallationAttributes(
            InstallationAttribute(productTypeId = ProductTypeId("productTypeId"), attributes = mapOf())
        )
    }

    @Test
    fun testDeleteInstallationAttributes() {
        val productTypeId = ProductTypeId("productTypeId")
        val attributes = mapOf(
            "param1" to "value1",
            "param2" to "value2"
        )
        val installationAttribute = InstallationAttribute(productTypeId = productTypeId, attributes = attributes)
        val purchaseOrder = buildPurchaseOrder(
            installationAttribute = installationAttribute
        )

        purchaseOrder.deleteInstallationAttributes(productTypeId)

        assertNull(purchaseOrder.installationAttributes[productTypeId])
    }

    @Test(expected = BusinessException::class)
    fun testDeleteInstallationAttributesOfInvalidPurchaseOrderStatus() {
        val productTypeId = ProductTypeId("productTypeId")
        val purchaseOrder = buildPurchaseOrder(
            status = PurchaseOrderStatus.COMPLETED,
            installationAttribute = InstallationAttribute(productTypeId = productTypeId, attributes = mapOf())
        )
        purchaseOrder.deleteInstallationAttributes(productTypeId)
    }

    @Test(expected = NotFoundException::class)
    fun testDeleteInstallationAttributesOfInvalidProductTypeId() {
        val productTypeId = ProductTypeId("productTypeId")
        val purchaseOrder = buildPurchaseOrder(
            installationAttribute = InstallationAttribute(productTypeId = productTypeId, attributes = mapOf())
        )
        purchaseOrder.deleteInstallationAttributes(ProductTypeId("invalidProductTypeId"))

    }


    @Test
    fun testInstallationAttributesFor() {
        val productTypeId = UUID.randomUUID().toString()
        val purchaseOrder = buildPurchaseOrder(
            installationAttribute = installationAttribute(productTypeId = productTypeId),
            type = PurchaseOrderType.JOIN
        )

        val installationAttributes = purchaseOrder.installationAttributesFor(productTypeId)

        assertEquals("value1", installationAttributes["param1"] as String)
        assertEquals("value2", installationAttributes["param2"] as String)
    }

}
