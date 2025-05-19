package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckout
import br.com.zup.realwave.sales.manager.domain.service.PurchaseOrderCheckoutResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CheckoutFactory @Autowired constructor(
    val purchaseOrderCheckoutService: PurchaseOrderCheckoutService,
    val purchaseOrderCouponCheckoutService: PurchaseOrderCouponCheckoutService
) : PurchaseOrderCheckoutResolver {

    override fun resolve(purchaseOrder: PurchaseOrder): PurchaseOrderCheckout {
        return when (purchaseOrder.type) {
            PurchaseOrderType.COUPON -> {
                purchaseOrderCouponCheckoutService
            }
            else -> {
                purchaseOrderCheckoutService
            }
        }

    }

}
