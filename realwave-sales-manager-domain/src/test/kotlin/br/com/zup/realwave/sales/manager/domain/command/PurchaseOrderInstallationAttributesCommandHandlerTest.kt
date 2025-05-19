package br.com.zup.realwave.sales.manager.domain.command

import br.com.zup.eventsourcing.core.Repository
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.commandhandler.PurchaseOrderInstallationAttributesCommandHandler
import br.com.zup.test.realwave.sales.manager.domain.buildPurchaseOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PurchaseOrderInstallationAttributesCommandHandlerTest {

    val repository = mockk<Repository<PurchaseOrder>>(relaxed = true)

    val installationAttributesCommandHandler = PurchaseOrderInstallationAttributesCommandHandler()
        .apply {
            this.repositoryManager = repository
        }

    @Test
    fun updateInstallationAttributes() {
        val purchaseOrder = buildPurchaseOrder(type = PurchaseOrderType.JOIN)
        val installationAttribute = buildInstallationAttribute()
        val productTypeId = installationAttribute.productTypeId
        val command = UpdateInstallationAttributesCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            installationAttribute = installationAttribute
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        installationAttributesCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertEquals(expected = installationAttribute, actual = purchaseOrder.installationAttributes[productTypeId])

    }

    @Test
    fun deleteInstallationAttributes() {
        val installationAttribute = buildInstallationAttribute()
        val purchaseOrder = buildPurchaseOrder(
            type = PurchaseOrderType.JOIN,
            installationAttribute = installationAttribute
        )
        val productTypeId = installationAttribute.productTypeId
        val command = DeleteInstallationAttributesCommand(
            id = PurchaseOrderId(purchaseOrder.idAsString()),
            productTypeId = productTypeId
        )

        every { repository.get(purchaseOrder.id) } returns purchaseOrder

        installationAttributesCommandHandler.handle(command)

        verify { repository.save(purchaseOrder) }

        assertNull(actual = purchaseOrder.installationAttributes[productTypeId])

    }

    private fun buildInstallationAttribute() =
        InstallationAttribute(
            productTypeId = ProductTypeId("product-type-id"),
            attributes = mapOf("a" to "b")
        )

}
