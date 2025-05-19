package br.com.zup.realwave.sales.manager.domain.commandhandler

import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.command.UpdateSegmentationCommand
import br.com.zup.realwave.sales.manager.domain.updateSegmentation
import org.springframework.stereotype.Service

@Service
class PurchaseOrderSegmentationCommandHandler : BaseCommandHandler() {

    fun handle(command: UpdateSegmentationCommand): PurchaseOrderId {
        log.debug(
            "Received command to update segmentation [{}] in event store to purchaseOrderId: {} to tenant [{}]",
            command.segmentation, command.id
        )
        withPurchaseOrder(command.id) { purchaseOrder ->
            purchaseOrder.updateSegmentation(command.segmentation)
        }
        log.debug("Updated segmentation [{}] to purchaseOrderId: {}", command.segmentation, command.id)
        return command.id
    }

}
