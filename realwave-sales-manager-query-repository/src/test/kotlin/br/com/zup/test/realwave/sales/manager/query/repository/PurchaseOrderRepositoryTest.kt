package br.com.zup.test.realwave.sales.manager.query.repository

import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.CatalogOfferType
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.CustomFields
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Freight
import br.com.zup.realwave.sales.manager.domain.InstallationAttribute
import br.com.zup.realwave.sales.manager.domain.Item
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OfferFields
import br.com.zup.realwave.sales.manager.domain.OfferValidity
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.Price
import br.com.zup.realwave.sales.manager.domain.PricePerPeriod
import br.com.zup.realwave.sales.manager.domain.ProductTypeId
import br.com.zup.realwave.sales.manager.domain.Protocol
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.domain.Subscription
import br.com.zup.realwave.sales.manager.domain.repository.PaymentRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderItemRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderVersionException
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.infrastructure.valueToTree
import br.com.zup.test.realwave.sales.manager.query.repository.config.RepositoryBaseTest
import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PurchaseOrderRepositoryTest : RepositoryBaseTest() {

    @Autowired
    private lateinit var purchaseOrderRepository: PurchaseOrderRepository

    @Autowired
    private lateinit var purchaseOrderItemRepository: PurchaseOrderItemRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Test
    fun savePurchaseOrderTest() {
        val rowsSaved = purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = PurchaseOrderId(),
            type = PurchaseOrderType.JOIN,
            channelCreate = Channel("APP"),
            callback = null,
            customer = null,
            version = 0
        )

        assertEquals(1, rowsSaved, "Purchase Order not created")
    }

    @Test
    fun savePurchaseOrder_WithCallback() {
        val rowsSaved = purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = PurchaseOrderId(),
            type = PurchaseOrderType.JOIN,
            channelCreate = Channel("APP"),
            callback = Callback("http://localhost:8080/callback", null),
            customer = null,
            version = 0
        )

        assertEquals(1, rowsSaved, "Purchase Order not created")
    }

    @Test
    fun updateSegmentationTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateSegmentation(purchaseOrderId, getSegmentation(), version++)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateSegmentationFailVersionLargerTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateSegmentation(purchaseOrderId, getSegmentation(), (version + 5))
    }

    @Test
    fun ignoredUpdateSegmentationVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateSegmentation(purchaseOrderId, getSegmentation(), version = 2)
        assertEquals(0, rowsUpdated, "Segmentation updated")
    }

    @Test
    fun ignoredUpdateSegmentationVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateSegmentation(purchaseOrderId, getSegmentation(), version = 3)
        assertEquals(0, rowsUpdated, "Segmentation updated")
    }

    @Test
    fun updateMgmTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val mgm = getMgm()
        val rowsUpdated = purchaseOrderRepository.updateMgm(purchaseOrderId, mgm, version++)

        assertEquals(1, rowsUpdated, "MGM not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(mgm.code, purchaseOrderSelected.mgm!!.code)
    }

    @Test
    fun updateProtocol() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val protocol = Protocol("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val rowsUpdated = purchaseOrderRepository.updateProtocol(purchaseOrderId, protocol, version++)

        assertEquals(1, rowsUpdated, "Protocol not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(protocol.value, purchaseOrderSelected.protocol)
    }

    @Test
    fun updatePurchaseOrderType() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val purchaseOrderType = PurchaseOrderType.BUY
        val rowsUpdated = purchaseOrderRepository.updatePurchaseOrderType(purchaseOrderId, purchaseOrderType, version++)

        assertEquals(1, rowsUpdated, "Purchase Order Type not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(PurchaseOrderType.BUY, purchaseOrderSelected.type)
    }


    @Test
    fun updateSubscription() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val subscription = Subscription("C57F4B65-0C93-46F1-87B8-D47EBADFF57F")
        val rowsUpdated = purchaseOrderRepository.updateSubscription(purchaseOrderId, subscription, version++)

        assertEquals(1, rowsUpdated, "Subscription not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(subscription.id, purchaseOrderSelected.subscriptionId)
    }

    @Test
    fun updateType() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val type = PurchaseOrderType.CHANGE
        val rowsUpdated = purchaseOrderRepository.updatePurchaseOrderType(purchaseOrderId, type, version++)

        assertEquals(1, rowsUpdated, "Purchase Order Type not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(type, purchaseOrderSelected.type)
    }


    @Test(expected = PurchaseOrderVersionException::class)
    fun updateMgmFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateMgm(purchaseOrderId, getMgm(), (version + 5))
    }

    @Test
    fun ignoredUpdateMgmVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateMgm(purchaseOrderId, getMgm(), version = 2)
        assertEquals(0, rowsUpdated, "Mgm updated")
    }

    @Test
    fun ignoredUpdateMgmVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateMgm(purchaseOrderId, getMgm(), version = 1)
        assertEquals(0, rowsUpdated, "Mgm updated")
    }


    @Test
    fun updateOnBoardingSaleWithoutCustomTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId,
            getOnBoardingSaleWithoutCustom(), version++
        )
        assertEquals(1, rowsUpdated, "OnBoardingSale not updated")
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateOnBoardingSaleWithoutCustomFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId,
            getOnBoardingSaleWithoutCustom(), (version + 5)
        )
    }

    @Test
    fun ignoredUpdateOnBoardingSaleVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId,
            getOnBoardingSaleWithoutCustom(), version = 2
        )
        assertEquals(0, rowsUpdated, "OnBoardingSale updated")
    }

    @Test
    fun ignoredUpdateOnBoardingSaleVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId,
            getOnBoardingSaleWithoutCustom(), version = 1
        )
        assertEquals(0, rowsUpdated, "OnBoardingSale updated")
    }


    @Test
    fun updateOnBoardingSaleWithCustomTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId, getOnBoardingSaleWithCustom()
            , (version++)
        )

        assertEquals(1, rowsUpdated, "OnBoardingSale not updated")
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateOnBoardingSaleWithCustomFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId, getOnBoardingSaleWithCustom()
            , (version + 5)
        )
    }

    @Test
    fun ignoredUpdateOnBoardingSaleWithCustomVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId, getOnBoardingSaleWithCustom(),
            version = 2
        )
        assertEquals(0, rowsUpdated, "OnBoardingSale updated")
    }

    @Test
    fun ignoredUpdateOnBoardingSaleWithCustomVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateOnBoardingSale(
            purchaseOrderId,
            getOnBoardingSaleWithCustom(), version = 1
        )
        assertEquals(0, rowsUpdated, "OnBoardingSale updated")
    }

    @Test
    fun updateCustomerTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)
        assertEquals(1, rowsUpdated, "Customer not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(customer, purchaseOrderSelected.customer)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateCustomerFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateCustomer(purchaseOrderId, getCustomer(), (version + 5))
    }

    @Test
    fun ignoredUpdateCustomerVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, getCustomer(), version = 2)
        assertEquals(0, rowsUpdated, "Customer updated")
    }

    @Test
    fun ignoredUpdateCustomerVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, getCustomer(), version = 1)
        assertEquals(0, rowsUpdated, "Customer updated")
    }

    @Test
    fun updateCouponTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val coupon = getCoupon()
        val rowsUpdated = purchaseOrderRepository.updateCoupon(purchaseOrderId, coupon, version++)
        assertEquals(1, rowsUpdated, "CouponCode not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(coupon.code, purchaseOrderSelected.coupon!!.code)
    }

    @Test
    fun updateStatus() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val rowsUpdated = purchaseOrderRepository.updateStatus(purchaseOrderId, PurchaseOrderStatus.CANCELED, version++)
        assertEquals(1, rowsUpdated, "Status Not Updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(PurchaseOrderStatus.CANCELED, purchaseOrderSelected.status)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateCouponFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateCoupon(purchaseOrderId, getCoupon(), (version + 5))
    }

    @Test
    fun ignoredUpdateCouponVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateCoupon(purchaseOrderId, getCoupon(), version = 2)
        assertEquals(0, rowsUpdated, "Coupon updated")
    }

    @Test
    fun ignoredUpdateCouponVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateCoupon(purchaseOrderId, getCoupon(), version = 1)
        assertEquals(0, rowsUpdated, "Coupon updated")
    }

    @Test
    fun updateFreightTest() {
        var version = 0L

        val purchaseOrderId = createPurchaseOrder(version++)

        val freight = Freight(
            type = Freight.Type("BR"),
            price = Price(
                currency = "BRL",
                amount = 2990,
                scale = 2
            ),
            address = Freight.Address(
                city = "Uberlândia",
                complement = "7o. Andar",
                country = "Brazil",
                district = "Tibery",
                name = "ZUP",
                state = "MG",
                street = "Av Rondon Pacheco",
                zipCode = "38400000",
                number = "2345"
            ),
            deliveryTotalTime = Freight.DeliveryTotalTime(3)
        )

        val rowsUpdated = purchaseOrderRepository.updateFreight(purchaseOrderId, freight, version)

        assertEquals(1, rowsUpdated)

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(freight.address.city, purchaseOrderSelected.freight!!.address.city)
        assertEquals(freight.type, purchaseOrderSelected.freight!!.type)
    }

    @Test
    fun updatePaymentTest() {
        var version = 0L

        val purchaseOrderId = createPurchaseOrder(version++)
        val paymentMean = Payment(
            listOf(
                Payment.PaymentMethod(
                    method = "CREDIT_CARD",
                    methodId = "card-id",
                    price = null,
                    customFields = buildAttributesToPayments()
                )
            )
        )

        val rowsUpdated = purchaseOrderRepository.updatePayment(purchaseOrderId, paymentMean, version++)
        assertEquals(1, rowsUpdated, "Payment not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(paymentMean.methods[0].method, purchaseOrderSelected.payment.methods[0].method)
        assertEquals(paymentMean.methods[0].methodId, purchaseOrderSelected.payment.methods[0].methodId)
        assertNotNull(paymentMean.methods.first().customFields)
    }

    @Test
    fun `update payment with description`() {
        var version = 0L

        val purchaseOrderId = createPurchaseOrder(version++)
        val payment = Payment(description = Payment.Description("The payment description"))

        val rowsUpdated = purchaseOrderRepository.updatePayment(purchaseOrderId, payment, version++)
        assertEquals(1, rowsUpdated, "Payment not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(payment.description, purchaseOrderSelected.payment.description)
    }

    @Test
    fun updatePaymentCustomFieldsTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val paymentCustomFields = Payment(
            methods = listOf(
                Payment.PaymentMethod(
                    method = "CREDIT_CARD",
                    methodId = "card-id",
                    customFields = buildAttributesToPayments()
                )
            )
        )

        val rowsUpdated = purchaseOrderRepository.updatePayment(purchaseOrderId, paymentCustomFields, version++)
        assertEquals(1, rowsUpdated, "Payment not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            paymentCustomFields.methods.first().customFields,
            purchaseOrderSelected.payment.methods.first().customFields
        )
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updatePaymentMeanFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updatePayment(
            purchaseOrderId, Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id"))), (version + 5)
        )
    }

    @Test
    fun ignoredUpdatePaymentMeanVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updatePayment(
            purchaseOrderId,
            Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id"))),
            version = 2
        )
        assertEquals(0, rowsUpdated, "PaymentMean updated")
    }

    @Test
    fun ignoredUpdatePaymentMeanVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updatePayment(
            purchaseOrderId,
            Payment(listOf(Payment.PaymentMethod("CREDIT_CARD", "card-id"))),
            version = 1
        )
        assertEquals(0, rowsUpdated, "PaymentMean updated")
    }

    @Test
    fun deletePurchaseOrderTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val rowsDeleted = purchaseOrderRepository.deletePurchaseOrder(purchaseOrderId, version++)
        assertEquals(1, rowsDeleted)

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(PurchaseOrderStatus.DELETED.name, purchaseOrderSelected.status.name)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun deletePurchaseOrderFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        purchaseOrderRepository.deletePurchaseOrder(purchaseOrderId, (version + 5))
    }

    @Test
    fun ignoredDeletePurchaseOrderVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.deletePurchaseOrder(purchaseOrderId, version = 2)
        assertEquals(0, rowsUpdated, "PurchaseOrder deleted")
    }

    @Test
    fun ignoredDeletePurchaseOrderVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.deletePurchaseOrder(purchaseOrderId, version = 1)
        assertEquals(0, rowsUpdated, "PurchaseOrder deleted")
    }


    @Test
    fun updateInstallationAttributesTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val installationAttributes = getInstallationAttributesFix()
        val rowsUpdated = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributes,
            version = version++
        )
        assertEquals(1, rowsUpdated, "Installation Attributes not updated")

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            installationAttributes, purchaseOrderSelected.installationAttributes.get(
                key =
                installationAttributes.productTypeId
            )
        )
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun updateInstallationAttributesFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = getInstallationAttributesFix(),
            version = (version + 5)
        )
    }

    @Test
    fun ignoredUpdateInstallationAttributesVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val rowsUpdated = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = getInstallationAttributesFix(),
            version = 2
        )
        assertEquals(0, rowsUpdated, "InstallationAttributes updated")
    }

    @Test
    fun ignoredUpdateInstallationAttributesVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val rowsUpdated = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = getInstallationAttributesFix(),
            version = 1
        )
        assertEquals(0, rowsUpdated, "InstallationAttributes updated")
    }

    @Test
    fun deleteInstallationAttributesTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val installationAttributesFix = getInstallationAttributesFix()
        var rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesFix, version = version++
        )
        assertEquals(1, rows, "Installation Attributes  Fix not updated")
        val installationsAttributesMobile = getInstallationAttributesMobile()
        rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationsAttributesMobile, version = version++
        )
        assertEquals(1, rows, "Installation Attributes Mobile not updated")
        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )
        assertEquals(
            installationsAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationsAttributesMobile.productTypeId]
        )

        rows = purchaseOrderRepository.deleteInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            productTypeId = installationAttributesFix.productTypeId,
            version = version++
        )
        assertEquals(1, rows, "Installation Attributes Fix not deleted")
        val purchaseOrderDeleted = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderDeleted.id.value)
        assertNull(purchaseOrderDeleted.installationAttributes[installationAttributesFix.productTypeId])
        assertEquals(
            installationsAttributesMobile,
            purchaseOrderDeleted.installationAttributes[installationsAttributesMobile.productTypeId]
        )

    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun deleteInstallationAttributesFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val installationAttributesFix = getInstallationAttributesFix()
        var rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesFix,
            version = version++
        )
        assertEquals(1, rows, "Installation Attributes Fix not updated")
        val installationAttributesMobile = getInstallationAttributesMobile()
        rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesMobile,
            version = version++
        )
        assertEquals(1, rows, "Installation Attributes Mobile not updated")
        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )
        assertEquals(
            installationAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId]
        )

        purchaseOrderRepository.deleteInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            productTypeId = installationAttributesFix.productTypeId,
            version = (version + 5)
        )
    }

    @Test
    fun ignoredDeleteInstallationAttributesVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 0)
        val installationAttributesFix = getInstallationAttributesFix()
        var rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesFix,
            version = 1
        )
        assertEquals(1, rows, "Installation Attributes Fix not updated")
        val installationAttributesMobile = getInstallationAttributesMobile()
        rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesMobile,
            version = 2
        )
        assertEquals(1, rows, "Installation Attributes Mobile not updated")
        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )
        assertEquals(
            installationAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId]
        )

        val rowsUpdated = purchaseOrderRepository.deleteInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            productTypeId = installationAttributesFix.productTypeId,
            version = 1
        )
        assertEquals(0, rowsUpdated, "InstallationAttributes deleted")
    }

    @Test
    fun ignoredDeleteInstallationAttributesVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 0)
        val installationAttributesFix = getInstallationAttributesFix()
        var rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesFix,
            version = 1
        )
        assertEquals(1, rows, "Installation Attributes Fix not updated")
        val installationAttributesMobile = getInstallationAttributesMobile()
        rows = purchaseOrderRepository.updateInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            installationAttribute = installationAttributesMobile,
            version = 2
        )
        assertEquals(1, rows, "Installation Attributes Mobile not updated")
        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!
        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(
            installationAttributesFix,
            purchaseOrderSelected.installationAttributes[installationAttributesFix.productTypeId]
        )
        assertEquals(
            installationAttributesMobile,
            purchaseOrderSelected.installationAttributes[installationAttributesMobile.productTypeId]
        )

        val rowsUpdated = purchaseOrderRepository.deleteInstallationAttributes(
            purchaseOrderId = purchaseOrderId.toString(),
            productTypeId = installationAttributesFix.productTypeId,
            version = 2
        )
        assertEquals(0, rowsUpdated, "InstallationAttributes deleted")
    }

    @Test
    fun addItemTest() {
        var version1 = 0L
        var version2 = 0L
        val id = createPurchaseOrder(version1++)
        val item = getItem()

        val id2 = createPurchaseOrder(version2++)
        val item2 = getItem()

        assertEquals(1, purchaseOrderRepository.addItem(id, item, version1++), "Item not Updated")
        assertEquals(1, purchaseOrderRepository.addItem(id2, item2, version2++), "Item not Updated")

        val savedItem = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(savedItem)
        assertEquals(savedItem?.catalogOfferType, CatalogOfferType("TEST"))
        assertEquals(item.offerFields.value, savedItem?.offerFields!!.value)
        assertEquals(item.offerItems.first().catalogOfferItemId, savedItem.offerItems.first().catalogOfferItemId)
    }

    @Test
    fun `add item with prices per period`() {
        var version1 = 0L
        val id = createPurchaseOrder(version1++)
        val item = getItem(pricesPerPeriod = pricesPerPeriod())

        assertEquals(1, purchaseOrderRepository.addItem(id, item, version1), "Item not Updated")

        val savedItem = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(savedItem)
        assertEquals(savedItem?.catalogOfferType, CatalogOfferType("TEST"))
        assertEquals(item.offerFields.value, savedItem?.offerFields!!.value)
        assertEquals(item.offerItems.first().catalogOfferItemId, savedItem.offerItems.first().catalogOfferItemId)
        assertTrue(savedItem.pricesPerPeriod.isNotEmpty())
    }

    @Test
    fun updateItem() {

        var version = 0L
        val id = createPurchaseOrder(version++)
        val item = getItem()

        purchaseOrderRepository.addItem(id, item, version++)

        val getItem = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(getItem)

        val itemToUpdate = item.copy(validity = OfferValidity("DAY", 50, false))
        purchaseOrderRepository.updateItem(purchaseOrderId = id, item = itemToUpdate, version = version++)
        assertNotNull(itemToUpdate)

        val itemUpdated = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(itemUpdated)
        assertEquals(itemUpdated!!.validity.period, "DAY")
        assertEquals(itemUpdated.validity.duration, 50)
        assertEquals(itemUpdated.validity.unlimited, false)

        assertEquals(item, itemUpdated)

    }

    @Test
    fun `update item with prices per period`() {
        var version = 0L
        val id = createPurchaseOrder(version++)
        val item = getItem()

        purchaseOrderRepository.addItem(id, item, version++)

        val getItem = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(getItem)
        assertTrue(getItem!!.pricesPerPeriod.isEmpty())

        val itemToUpdate = item.copy(
            validity = OfferValidity("DAY", 50, false),
            pricesPerPeriod = pricesPerPeriod()
        )
        purchaseOrderRepository.updateItem(purchaseOrderId = id, item = itemToUpdate, version = version)
        assertNotNull(itemToUpdate)

        val itemUpdated = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertNotNull(itemUpdated)
        assertEquals(itemUpdated!!.validity.period, "DAY")
        assertEquals(itemUpdated.validity.duration, 50)
        assertEquals(itemUpdated.validity.unlimited, false)

        assertEquals(item, itemUpdated)
        assertTrue(itemUpdated.pricesPerPeriod.isNotEmpty())
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun addItemFailVersionTest() {
        var version = 0L
        val id = createPurchaseOrder(version++)
        val item = getItem()

        purchaseOrderRepository.addItem(id, item, (version + 5))
    }

    @Test
    fun ignoredUpdateItemVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 3)
        val item = getItem()
        val rowsUpdated = purchaseOrderRepository.addItem(purchaseOrderId = purchaseOrderId, item = item, version = 2)
        assertEquals(0, rowsUpdated, "Item updated")
    }

    @Test
    fun ignoredUpdateItemVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)
        val item = getItem()
        val rowsUpdated = purchaseOrderRepository.addItem(purchaseOrderId = purchaseOrderId, item = item, version = 1)
        assertEquals(0, rowsUpdated, "Item updated")
    }

    @Test
    fun addingTheSameItemShouldUpdateIt() {
        var version = 0L
        val id = createPurchaseOrder(version++)
        val item = getItem()

        assertEquals(1, purchaseOrderRepository.addItem(id, item, version++), "Item not Updated")
        assertEquals(1, purchaseOrderRepository.addItem(id, getItem(), version++), "Item not Updated")

        val savedItem = purchaseOrderItemRepository.findOne(purchaseOrderId = id, itemId = item.id)
        assertEquals(savedItem?.catalogOfferType, CatalogOfferType("TEST"))
        assertEquals(savedItem?.offerFields!!.value!!["attr1"].asText(), "new value")
    }

    @Test
    fun removeItemFromOrder() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val purchaseOrderItem = createPurchaseOrderItem(purchaseOrderId, version++)

        purchaseOrderRepository.removeItem(purchaseOrderId, purchaseOrderItem.id, version++)

        val savedItem = purchaseOrderItemRepository.findOne(
            purchaseOrderId = purchaseOrderId,
            itemId = purchaseOrderItem.id
        )

        assertNull(savedItem)
    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun removeItemFromOrderFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val purchaseOrderItem = createPurchaseOrderItem(purchaseOrderId, version++)

        purchaseOrderRepository.removeItem(purchaseOrderId, purchaseOrderItem.id, (version + 5))
    }

    @Test
    fun ignoredDeleteItemVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 0)
        val purchaseOrderItem = createPurchaseOrderItem(purchaseOrderId, version = 1)

        val rows = purchaseOrderRepository.removeItem(purchaseOrderId, purchaseOrderItem.id, version = 0)
        assertEquals(0, rows, "Item deleted")
    }

    @Test
    fun ignoredDeleteItemVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 0)
        val purchaseOrderItem = createPurchaseOrderItem(purchaseOrderId, version = 1)

        val rows = purchaseOrderRepository.removeItem(purchaseOrderId, purchaseOrderItem.id, version = 1)
        assertEquals(0, rows, "Item deleted")
    }

    @Test
    fun checkoutOrderTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val paymentMean = Payment(
            listOf(
                Payment.PaymentMethod(
                    method = "CREDIT_CARD",
                    methodId = "payment-id",
                    price = null,
                    customFields = buildAttributesToPayments()
                ),
                Payment.PaymentMethod(
                    method = "CREDIT_CARD",
                    methodId = "payment-id-1",
                    price = null,
                    customFields = buildAttributesToPayments()
                ),
                Payment.PaymentMethod(
                    method = "CREDIT_CARD",
                    methodId = "payment-id-2",
                    price = null,
                    customFields = buildAttributesToPayments()
                )
            )
        )

        val rowsUpdated = purchaseOrderRepository.updatePayment(purchaseOrderId, paymentMean, version++)
        assertEquals(1, rowsUpdated)


        val rowsCheckedOut = purchaseOrderRepository.checkOutPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            channelCheckout = Channel("APP"),
            securityCodeInformed = listOf(SecurityCodeInformed("payment-id", true)),
            version = version++
        )

        val rowsCheckedOut1 = purchaseOrderRepository.checkOutPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            channelCheckout = Channel("APP"),
            securityCodeInformed = listOf(SecurityCodeInformed("payment-id-2", true)),
            version = version++
        )

        assertEquals(1, rowsCheckedOut1)

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(true, purchaseOrderSelected.payment.methods[0].securityCodeInformed)
        assertEquals(false, purchaseOrderSelected.payment.methods[1].securityCodeInformed)
        assertEquals(PurchaseOrderStatus.CHECKED_OUT.name, purchaseOrderSelected.status.name)

    }

    @Test(expected = PurchaseOrderVersionException::class)
    fun checkoutOrderFailVersionTest() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        purchaseOrderRepository.checkOutPurchaseOrder(purchaseOrderId, Channel("APP"), (version + 5))
    }

    @Test
    fun ignoredCheckoutOrderVersionSmallerTest() {
        val purchaseOrderId = createPurchaseOrder(version = 1)

        val rows = purchaseOrderRepository.checkOutPurchaseOrder(purchaseOrderId, Channel("APP"), version = 0)
        assertEquals(0, rows, "CheckoutOrder updated")
    }

    @Test
    fun ignoredCheckoutOrderVersionEqualsTest() {
        val purchaseOrderId = createPurchaseOrder(version = 0)

        val rows = purchaseOrderRepository.checkOutPurchaseOrder(purchaseOrderId, Channel("APP"), version = 0)
        assertEquals(0, rows, "CheckoutOrder updated")
    }

    @Test
    fun findByCustomerId() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrderSelected = purchaseOrderRepository.find(customer, null, null, null)
        assertEquals("276e6413-f176-4f84-b9cc-2b59ee09710e", purchaseOrderSelected[0].customer!!.id)
    }

    @Test
    fun findByCustomerIdAndStatus() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrderSelected = purchaseOrderRepository.find(customer, "OPENED", null, null)
        assertEquals(PurchaseOrderStatus.OPENED.name, purchaseOrderSelected[0].status.name)
        assertEquals("276e6413-f176-4f84-b9cc-2b59ee09710e", purchaseOrderSelected[0].customer!!.id)
    }

    @Test
    fun findByCustomerIdAndBetweenDates() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val purchaseOrder = purchaseOrderRepository.find(purchaseOrderId)
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrderSelected = purchaseOrderRepository.find(
            customer, null, purchaseOrder!!.createdAt,
            purchaseOrder.createdAt
        )
        assertEquals("276e6413-f176-4f84-b9cc-2b59ee09710e", purchaseOrderSelected[0].customer!!.id)
    }

    @Test
    fun findByCustomerIdAndStatusAndBetweenDates() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrder = purchaseOrderRepository.find(purchaseOrderId)

        val purchaseOrderSelected = purchaseOrderRepository.find(
            customer, null, purchaseOrder!!.createdAt,
            purchaseOrder.createdAt
        )
        assertEquals(PurchaseOrderStatus.OPENED.name, purchaseOrderSelected[0].status.name)
        assertEquals("276e6413-f176-4f84-b9cc-2b59ee09710e", purchaseOrderSelected[0].customer!!.id)
    }

    @Test
    fun findByProtocol() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)
        val protocol = UUID.randomUUID().toString()
        purchaseOrderRepository.updateProtocol(purchaseOrderId, Protocol(protocol), version++)

        val purchaseOrder = purchaseOrderRepository.find(purchaseOrderId)

        val purchaseOrderSelected = purchaseOrderRepository.findByProtocol(Protocol(protocol))!!
        assertEquals(protocol, purchaseOrderSelected.protocol)
    }

    @Test
    fun `update method id with checkout boleto`(){
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val paymentMean = Payment(
            listOf(
                Payment.PaymentMethod(
                    method = "BOLETO",
                    methodId = null,
                    price = null,
                    customFields = buildAttributesToPayments()
                )
            )
        )

        val rowsPaymentUpdated = purchaseOrderRepository.updatePayment(purchaseOrderId, paymentMean, version++)

        assertEquals(1, rowsPaymentUpdated)

        val rowsCheckedOut = purchaseOrderRepository.checkOutPurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            channelCheckout = Channel("APP"),
            securityCodeInformed = listOf(SecurityCodeInformed("payment-id", true)),
            version = version++
        )

        assertEquals(1, rowsCheckedOut)

        val rowsMethodIdUpdated = paymentRepository.updateMethodId(purchaseOrderId, "BOL-6166f38a-4c6b-4200-a6cb-37cad92b4376")

        assertEquals(1,  rowsMethodIdUpdated)

        assertNotNull(paymentRepository.findAll(purchaseOrderId).first().methodId)
    }

    @Test
    fun shouldNotFindByCustomerIdAndBetweenDatesOutOfRange() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrderSelected = purchaseOrderRepository.find(
            customer, "OPENED", LocalDateTime.now().toString(),
            LocalDateTime.now().toString()
        )
        assertEquals(0, purchaseOrderSelected.size)
    }

    @Test
    fun shouldNotFindByCustomerIdAndStatusCheckedOut() {
        var version = 0L
        val purchaseOrderId = createPurchaseOrder(version++)

        val customer = getCustomer()
        val rowsUpdated = purchaseOrderRepository.updateCustomer(purchaseOrderId, customer, version++)

        val purchaseOrderSelected = purchaseOrderRepository.find(customer, "CHECKED_OUT", null, null)
        assertEquals(0, purchaseOrderSelected.size)
    }

    private fun createPurchaseOrderItem(purchaseOrderId: PurchaseOrderId, version: Long): Item {
        val purchaseOrderItem = getItem()
        purchaseOrderRepository.addItem(purchaseOrderId, purchaseOrderItem, version)
        return purchaseOrderItem
    }

    private fun createPurchaseOrder(version: Long): PurchaseOrderId {
        val purchaseOrderId = PurchaseOrderId()
        purchaseOrderRepository.savePurchaseOrder(
            purchaseOrderId = purchaseOrderId,
            version = version,
            type = PurchaseOrderType.JOIN,
            channelCreate = Channel("APP"),
            customer = null,
            callback = null
        )
        return purchaseOrderId
    }

    private fun getSegmentation(): Segmentation =
        Segmentation("{}".jsonToObject(JsonNode::class.java))

    private fun getMgm(): Mgm {
        return Mgm(code = UUID.randomUUID().toString(), customFields = buildAttributes().valueToTree())
    }

    private fun getOnBoardingSaleWithoutCustom(): OnBoardingSale {
        val offerId = CatalogOfferId(UUID.randomUUID().toString())
        return OnBoardingSale(offerId, null)
    }

    private fun getOnBoardingSaleWithCustom(): OnBoardingSale {
        val obj = mapOf(
            "param1_obs" to "value1",
            "param2_obs" to "value2"
        )
        val offerId = CatalogOfferId(UUID.randomUUID().toString())
        return OnBoardingSale(offerId, buildAttributes(obj))
    }

    private fun getCustomer(): Customer {
        return Customer("276e6413-f176-4f84-b9cc-2b59ee09710e")
    }

    private fun getCoupon(): CouponCode {
        return CouponCode(code = UUID.randomUUID().toString(), customFields = buildAttributes().valueToTree())
    }

    private fun getInstallationAttributesFix(): InstallationAttribute {
        return InstallationAttribute(productTypeId = ProductTypeId("FIX"), attributes = buildAttributes())
    }

    private fun getInstallationAttributesMobile(): InstallationAttribute {
        return InstallationAttribute(productTypeId = ProductTypeId("MOBILE"), attributes = buildAttributes())
    }

    private fun buildAttributes(): Map<String, String> {
        return mapOf(
            "param1" to "value1",
            "param2" to "value2"
        )
    }

    private fun buildAttributesToPayments(): JsonNode {
        return buildAttributes(mapOf("paymentsCustomFields" to "payments-custom-fields"))
    }

    private fun buildAttributes(map: Map<String, Any>): JsonNode {
        return map.valueToTree()
    }

    private fun getItem(pricesPerPeriod: List<PricePerPeriod> = listOf()): Item {
        return Item(
            catalogOfferId = CatalogOfferId(UUID.randomUUID().toString()),
            catalogOfferType = CatalogOfferType("TEST"),
            price = Price(
                currency = "BRL",
                amount = 3799,
                scale = 2
            ),
            validity = OfferValidity(
                period = "DAY",
                duration = 30,
                unlimited = false
            ),
            offerFields = OfferFields(buildAttributes(mapOf("attr1" to "new value"))),
            customFields = CustomFields(buildAttributes(mapOf("attr1" to "new value"))),
            offerItems = listOf(
                Item.OfferItem(
                    catalogOfferItemId = Item.OfferItem.CatalogOfferItemId("id"),
                    price = Price(
                        "BRL",
                        1000,
                        2
                    )
                )
            ),
            pricesPerPeriod = pricesPerPeriod
        )
    }

    @Test
    fun updatePurchaseOrderReasonStatus() {
        var version = 0L
        var reason = Reason("CANCELED", null)
        val purchaseOrderId = createPurchaseOrder(version++)

        val rows = purchaseOrderRepository.updatePurchaseOrderReason(purchaseOrderId, reason, version++)
        assertEquals(1, rows)

        val purchaseOrderSelected = purchaseOrderRepository.find(purchaseOrderId)!!

        assertEquals(purchaseOrderId.value, purchaseOrderSelected.id.value)
        assertEquals(reason.code, purchaseOrderSelected.reason?.code)
    }

    private fun pricesPerPeriod() =
        listOf(
            PricePerPeriod(
                totalPrice = Price("BRL", 1000, 2),
                totalDiscountPrice = Price("BRL", 800, 2),
                totalPriceWithDiscount = Price("BRL", 200, 2),
                startAt = PricePerPeriod.StartAt(1),
                endAt = PricePerPeriod.EndAt(2),
                items = listOf()
            )
        )

}
