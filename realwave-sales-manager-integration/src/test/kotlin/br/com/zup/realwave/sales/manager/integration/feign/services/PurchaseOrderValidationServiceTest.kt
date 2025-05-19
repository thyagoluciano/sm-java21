package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.service.CatalogManagerService
import br.com.zup.realwave.sales.manager.domain.service.CouponService
import br.com.zup.realwave.sales.manager.domain.service.CustomerInfoService
import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrder
import br.com.zup.realwave.sales.manager.integration.buildPurchaseOrderCoupon
import br.com.zup.realwave.sales.manager.integration.offerItemWithProductId
import br.com.zup.realwave.sales.manager.integration.planOfferItems
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItem
import br.com.zup.realwave.sales.manager.integration.purchaseOrderItemWithProduct
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers.anyString
import org.mockito.Mockito
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PurchaseOrderValidationServiceTest {

    lateinit var memberGetMemberService: MemberGetMemberService
    lateinit var customerInfoService: CustomerInfoService
    lateinit var couponService: CouponService
    lateinit var catalogManagerService: CatalogManagerService
    lateinit var purchaseOrderService: PurchaseOrderValidationService

    @Before
    fun init() {
        memberGetMemberService = Mockito.mock(MemberGetMemberService::class.java)
        customerInfoService = Mockito.mock(CustomerInfoService::class.java)
        couponService = Mockito.mock(CouponService::class.java)
        catalogManagerService = Mockito.mock(CatalogManagerService::class.java)
        purchaseOrderService = PurchaseOrderValidationService(
            memberGetMemberService = memberGetMemberService,
            customerInfoService = customerInfoService,
            couponService = couponService,
            catalogManagerService = catalogManagerService
        )
    }

    @Test
    fun validateJoinPurchaseOrderWithoutMgmCode() {
        val purchaseOrder = buildPurchaseOrder()

        purchaseOrderService.validate(purchaseOrder)

        Mockito.verify(catalogManagerService).validateOffers(purchaseOrder.items)
        Mockito.verify(customerInfoService).validateCustomer(purchaseOrder.customer?.id!!)
        Mockito.verify(customerInfoService, Mockito.never()).validateProduct(anyString(), anyString())
        Mockito.verify(couponService, Mockito.never()).validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test
    fun validateJoinPurchaseOrderWithMgmCode() {
        val purchaseOrder = buildPurchaseOrder()

        purchaseOrder.mgm = Mgm(code = "code")

        purchaseOrderService.validate(purchaseOrder)

        Mockito.verify(memberGetMemberService).validate(purchaseOrder.mgm?.code)
        Mockito.verify(catalogManagerService).validateOffers(purchaseOrder.items)
        Mockito.verify(customerInfoService).validateCustomer(purchaseOrder.customer?.id!!)
        Mockito.verify(customerInfoService, Mockito.never()).validateProduct(anyString(), anyString())
        Mockito.verify(couponService, Mockito.never()).validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test(expected = BusinessException::class)
    fun joinPurchaseOrderWithProductId() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = offerItemWithProductId()
                )
            ),
            type = PurchaseOrderType.JOIN
        )
        purchaseOrderService.validate(purchaseOrder)
    }

    @Test
    fun validateCouponPurchaseOrder() {
        val purchaseOrder = buildPurchaseOrderCoupon()

        purchaseOrderService.validate(purchaseOrder)

        Mockito.verify(catalogManagerService, Mockito.never()).validateOffers(purchaseOrder.items)
        Mockito.verify(customerInfoService).validateCustomer(purchaseOrder.customer?.id!!)
        Mockito.verify(customerInfoService, Mockito.never()).validateProduct(anyString(), anyString())
        Mockito.verify(couponService).validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test
    fun validateBuyPurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder(
            type = PurchaseOrderType.BUY,
            items = listOf(purchaseOrderItemWithProduct())
        )

        purchaseOrderService.validate(purchaseOrder)

        Mockito.verify(catalogManagerService).validateOffers(purchaseOrder.items)
        Mockito.verify(customerInfoService).validateCustomer(purchaseOrder.customer?.id!!)
        Mockito.verify(customerInfoService).validateProduct(anyString(), anyString())
        Mockito.verify(couponService, Mockito.never()).validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test(expected = BusinessException::class)
    fun buyPurchaseOrderWithoutProductId() {
        val purchaseOrder = buildPurchaseOrder(
            type = PurchaseOrderType.BUY
        )

        purchaseOrderService.validate(purchaseOrder)
    }

    @Test
    fun validateChangePurchaseOrder() {
        val purchaseOrder = buildPurchaseOrder(
            type = PurchaseOrderType.CHANGE,
            items = listOf(purchaseOrderItemWithProduct()),
            subscription = "subscription"
        )

        purchaseOrderService.validate(purchaseOrder)

        Mockito.verify(catalogManagerService).validateOffers(purchaseOrder.items)
        Mockito.verify(customerInfoService).validateCustomer(purchaseOrder.customer?.id!!)
        Mockito.verify(customerInfoService).validateProduct(anyString(), anyString())
        Mockito.verify(couponService, Mockito.never()).validationPurchaseOrderCoupon(purchaseOrder)
    }

    @Test(expected = BusinessException::class)
    fun changePurchaseOrderMustHaveSubscription() {

        val purchaseOrder = buildPurchaseOrder(
            type = PurchaseOrderType.CHANGE,
            items = listOf(purchaseOrderItemWithProduct())
        )

        purchaseOrderService.validate(purchaseOrder)
    }

    @Test
    fun validPurchaseOrderWithProductIds() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(purchaseOrderItem(offerItems = offerItemWithProductId())),
            type = PurchaseOrderType.BUY
        )
        purchaseOrderService.validate(purchaseOrder)

        assertNotNull(purchaseOrder.items.first().offerItems[0].productId)
    }

    @Test
    fun validPurchaseOrderWithoutAnyProductId() {

        val purchaseOrderId = PurchaseOrderId()
        val purchaseOrder = buildPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            items = listOf(
                purchaseOrderItem(
                    offerItems = planOfferItems()
                )
            )
        )
        purchaseOrderService.validate(purchaseOrder)

        assertNull(purchaseOrder.items.first().offerItems[0].productId)
        assertNull(purchaseOrder.items.first().offerItems[1].productId)
    }


}
