package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmDeleted
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderMgmUpdated
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService

fun PurchaseOrder.updateMgm(mgm: Mgm, memberGetMemberService: MemberGetMemberService) {
    verifyPurchaseOrderIsOpen()
    validateMgm(mgm, memberGetMemberService)
    applyChange(PurchaseOrderMgmUpdated(id, mgm))
}

fun PurchaseOrder.deleteMgm() {
    verifyPurchaseOrderIsOpen()
    applyChange(PurchaseOrderMgmDeleted(id, null))
}
