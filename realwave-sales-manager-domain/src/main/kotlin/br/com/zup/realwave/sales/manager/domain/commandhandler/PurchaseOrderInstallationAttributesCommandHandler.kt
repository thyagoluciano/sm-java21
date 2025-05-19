package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.DeleteInstallationAttributesCommand
import br.com.zup.realwave.sales.manager.domain.command.UpdateInstallationAttributesCommand
import br.com.zup.realwave.sales.manager.domain.deleteInstallationAttributes
import br.com.zup.realwave.sales.manager.domain.updateInstallationAttributes
import org.springframework.stereotype.Service

@Service
class PurchaseOrderInstallationAttributesCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateInstallationAttributesCommand): PurchaseOrderId {
        log.debug(
            "Received command to update installation attributes [{}] " +
                    "in event store to purchaseOrderId: {} to tenant [{}]",
            command.installationAttribute,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateInstallationAttributes(command.installationAttribute)
        }
        log.debug(
            "Updated installation attributes [{}] to purchaseOrderId: {}",
            command.installationAttribute,
            command.id
        )
        return command.id
    }

    fun handle(command: DeleteInstallationAttributesCommand): PurchaseOrderId {
        log.debug(
            "Received command to delete installation attributes [{}] " +
                    "in event store to purchaseOrderId: {} to tenant [{}]",
            command.productTypeId,
            command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.deleteInstallationAttributes(command.productTypeId)
        }
        log.debug(
            "Deleted installation attributes [{}] to purchaseOrderId: {}",
            command.productTypeId,
            command.id
        )
        return command.id
    }

}
