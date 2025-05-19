package br.com.zup.realwave.sales.manager.domain

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.common.exception.handler.exception.NotFoundException
import br.com.zup.realwave.common.exception.handler.to.ResourceValue
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderValidator
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderValidationMethodsException

fun PurchaseOrder.validatePurchaseOrder(purchaseOrderValidator: PurchaseOrderValidator) {
    purchaseOrderValidator.validate(this)
}

fun PurchaseOrder.verifyPurchaseOrderIsOpen() {
    if (PurchaseOrderStatus.OPENED != status)
        throw BusinessException.of("purchaseOrderId", PurchaseOrderErrorCode.Companion.PURCHASE_ORDER_NOT_EXISTS)
}

fun PurchaseOrder.verifyPurchaseOrderType() {
    if (type != null) {
        throw BusinessException.of("type", PurchaseOrderErrorCode.PURCHASE_ORDER_TYPE_IS_ALREADY_DEFINED)
    }
}

fun PurchaseOrder.verifyInstallationAttributesConstainsProductTypeId(productTypeId: ProductTypeId) {
    if (!installationAttributes.containsKey(productTypeId)) {
        throw NotFoundException(ResourceValue("productTypeId", productTypeId.toString()))
    }
}

fun PurchaseOrder.hasCustomer() {
    if (this.customer == null) {
        log.debug("Purchase Order without customer. Purchase Order [{}]", this)
        throw BusinessException.of("customer", PurchaseOrderErrorCode.CUSTOMER_NOT_INFORMED)
    }
}

fun PurchaseOrder.canCheckout() {
    hasCustomer()
    if (this.payment.methods.isEmpty()) {
        log.debug("Purchase Order without payment information. Purchase Order [{}]", this)
        throw BusinessException.of("Payment", PurchaseOrderErrorCode.PAYMENT_NOT_FOUND)
    }
    if (this.items.isEmpty()) {
        log.debug("Purchase Order without items. Purchase Order [{}]", this)
        throw BusinessException.of("Items", PurchaseOrderErrorCode.ITEMS_NOT_INFORMED)
    }
    val duplicated =this.payment.methods.groupBy{ Pair(it.methodId,it.method) }.filter{ it.value.size > 1}

    if(duplicated.size > 0){
        log.debug("Purchase Order contain more than one payment method equals. Purchase Order [{}]", this)
        val methodsDuplicated = duplicated.values.flatten().distinctBy { Pair(it.methodId,it.method)}
        val listPayment: ArrayList<PurchaseOrderValidationMethodsException.PaymentMethod> = ArrayList<PurchaseOrderValidationMethodsException.PaymentMethod>()
        methodsDuplicated.forEach{
            listPayment.add(PurchaseOrderValidationMethodsException.PaymentMethod(it.method,it.methodId))
        }
        throw PurchaseOrderValidationMethodsException(PurchaseOrderErrorCode.MORE_ONE_PAYMENT_METHOD,listPayment)
    }



}

fun PurchaseOrder.canDelete() {
    if (PurchaseOrderStatus.OPENED != status) {
        throw BusinessException.of("purchaseOrderId", PurchaseOrderErrorCode.Companion.PURCHASE_ORDER_CANNOT_DELETE)
    }
}

fun validateMgm(mgm: Mgm?, memberGetMemberService: MemberGetMemberService) {
    memberGetMemberService.validate(mgm?.code)
}

fun PurchaseOrder.validateCoupon(coupon: CouponCode, couponService: CouponService): CouponCode {
    return couponService.validateCoupon(coupon, customer!!)
}

fun PurchaseOrder.validatePurchaseOrderTypeForCoupon() {
    if (!arrayOf(PurchaseOrderType.BUY, PurchaseOrderType.JOIN).contains(type)) {
        throw BusinessException.of("type", PurchaseOrderErrorCode.PURCHASE_ORDER_INVALID_TYPE)
    }
}
