package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Subscription

/**
 * Created by Danilo Paiva on 26/05/17
 */
interface PurchaseOrderRepository {

    fun find(purchaseOrderId: PurchaseOrderId): PurchaseOrder?

    fun findByProtocol(protocol: Protocol): PurchaseOrder?

    fun find(customer: Customer, status: String?, start: String?, end: String?): List<PurchaseOrder>

    fun savePurchaseOrder(
        purchaseOrderId: PurchaseOrderId,
        type: PurchaseOrderType?,
        channelCreate: Channel,
        callback: Callback?,
        customer: Customer?,
        version: Long
    ): Int

    fun updateSegmentation(purchaseOrderId: PurchaseOrderId, segmentation: Segmentation, version: Long): Int

    fun updateMgm(purchaseOrderId: PurchaseOrderId, mgm: Mgm?, version: Long): Int

    fun updateOnBoardingSale(purchaseOrderId: PurchaseOrderId, onBoardingSale: OnBoardingSale, version: Long): Int

    fun updateCustomer(purchaseOrderId: PurchaseOrderId, customer: Customer, version: Long): Int

    fun updateCoupon(purchaseOrderId: PurchaseOrderId, coupon: CouponCode, version: Long): Int

    fun updatePayment(purchaseOrderId: PurchaseOrderId, paymentList: Payment, version: Long): Int

    fun updateFreight(purchaseOrderId: PurchaseOrderId, freight: Freight, version: Long): Int

    fun updateInstallationAttributes(
        purchaseOrderId: String,
        installationAttribute: InstallationAttribute,
        version: Long
    ): Int

    fun deleteInstallationAttributes(purchaseOrderId: String, productTypeId: ProductTypeId, version: Long): Int

    fun deletePurchaseOrder(purchaseOrderId: PurchaseOrderId, version: Long): Int

    fun addItem(purchaseOrderId: AggregateId, item: Item, version: Long): Int

    fun removeItem(purchaseOrderId: AggregateId, itemId: Item.Id, version: Long): Int

    fun updateItem(purchaseOrderId: AggregateId, item: Item, version: Long): Int

    fun checkOutPurchaseOrder(
        purchaseOrderId: AggregateId,
        channelCheckout: Channel,
        version: Long,
        securityCodeInformed: List<SecurityCodeInformed> = emptyList()
    ): Int

    fun updateVersion(purchaseOrderId: AggregateId, version: Long): Int

    fun updateProtocol(purchaseOrderId: AggregateId, protocol: Protocol, version: Long): Int

    fun updateSubscription(purchaseOrderId: AggregateId, subscription: Subscription, version: Long): Int

    fun updatePurchaseOrderType(purchaseOrderId: AggregateId, purchaseOrderType: PurchaseOrderType?, version: Long): Int

    fun updatePurchaseOrderReason(purchaseOrderId: AggregateId, reason: Reason?, version: Long): Int

    fun updatePaymentDescription(
        purchaseOrderId: PurchaseOrderId,
        paymentDescription: Payment.Description?,
        version: Long
    ): Int

    fun updateStatus(purchaseOrderId: AggregateId, status: PurchaseOrderStatus, version: Long): Int

    fun updateSalesForce(purchaseOrderId: PurchaseOrderId, salesForce: SalesForce?, version: Long): Int

}
