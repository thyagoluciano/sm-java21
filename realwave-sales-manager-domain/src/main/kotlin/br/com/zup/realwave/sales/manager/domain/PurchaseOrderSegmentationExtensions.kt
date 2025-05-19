package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderSegmentationUpdated

fun PurchaseOrder.updateSegmentation(segmentation: Segmentation) {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderSegmentationUpdated(id, segmentation))
}
