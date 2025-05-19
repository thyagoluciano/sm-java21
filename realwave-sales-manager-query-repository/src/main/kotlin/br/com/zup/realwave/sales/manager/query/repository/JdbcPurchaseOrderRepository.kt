package br.com.zup.realwave.sales.manager.query.repository

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
import br.com.zup.realwave.sales.manager.domain.repository.InstallationAttributesRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderItemRepository
import br.com.zup.realwave.sales.manager.domain.repository.PurchaseOrderRepository
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderVersionException
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.query.repository.extractor.PurchaseOrderMapper
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Created by Danilo Paiva on 26/05/17
 */
@Repository
open class JdbcPurchaseOrderRepository
@Autowired constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val installationAttributesRepository: InstallationAttributesRepository,
    private val purchaseOrderItemRepository: PurchaseOrderItemRepository,
    private val customerOrderRepository: JdbcCustomerOrderRepository,
    private val paymentRepository: JdbcPaymentRepository,
    private val couponRepository: JdbcDiscountRepository,
    private val freightRepository: JdbcFreightRepository
) : PurchaseOrderRepository {

    private val log = LogManager.getLogger(this.javaClass)

    companion object {
        const val TABLE_NAME: String = "PURCHASE_ORDER"
        const val PURCHASE_ORDER_ID_COLUMN: String = "id"
        const val SEGMENTATION_COLUMN: String = "segmentation"
        const val MGM_CODE_COLUMN: String = "mgm_code"
        const val MGM_CUSTOM_FIELDS: String = "mgm_custom_fields"
        const val ON_BOARDING_SALE_OFFER_ID = "on_boarding_sale_offer_id"
        const val ON_BOARDING_SALE_CUSTOM_FIELDS = "on_boarding_sale_custom_fields"
        const val CUSTOMER_COLUMN = "customer"
        const val COUPON_CODE_COLUMN: String = "coupon_code"
        const val COUPON_CUSTOM_FIELDS: String = "coupon_custom_fields"
        const val STATUS_COLUMN = "status"
        const val CREATED_COLUMN: String = "created"
        const val UPDATED_COLUMN: String = "updated"
        const val VERSION_COLUMN: String = "version"
        const val PROTOCOL_COLUMN: String = "protocol"
        const val PURCHASE_ORDER_TYPE_COLUMN: String = "purchase_order_type"
        const val SUBSCRIPTION_ID_COLUMN: String = "subscription_id"
        const val CHANNEL_CREATE_COLUMN: String = "channel_create"
        const val CHANNEL_CHECKOUT_COLUMN: String = "channel_checkout"
        const val PURCHASE_ORDER_CALLBACK_URL: String = "callback"
        const val PURCHASE_ORDER_CALLBACK_HEADERS: String = "callback_headers"
        const val PURCHASE_ORDER_REASON: String = "reason"
        const val PAYMENT_DESC: String = "payment_description"
        const val SALES_FORCE_ID_COLUMN: String = "sales_force_id"
        const val SALES_FORCE_NAME_COLUMN: String = "sales_force_name"
    }

    override fun find(purchaseOrderId: PurchaseOrderId): PurchaseOrder? {
        val sql = "select * from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ?"
        val purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), purchaseOrderId.value)

        return when (purchaseOrders.size) {
            0 -> null
            1 -> {
                val purchaseOrder = purchaseOrders[0]
                buildFullPurchaseOrder(purchaseOrder)
            }
            else -> throw IncorrectResultSizeDataAccessException(1, purchaseOrders.size)
        }
    }

    private fun buildFullPurchaseOrder(purchaseOrder: PurchaseOrder): PurchaseOrder? {
        purchaseOrder.installationAttributes =
                installationAttributesRepository.findAll(purchaseOrder.id.value) as
                        HashMap<ProductTypeId, InstallationAttribute>
        purchaseOrder.items = purchaseOrderItemRepository.findByPurchaseOrderId(PurchaseOrderId(purchaseOrder.id.value))
            .toMutableSet()
        purchaseOrder.customerOrder = customerOrderRepository.findOne(purchaseOrder.id)
        purchaseOrder.payment.methods = paymentRepository.findAll(purchaseOrder.id)
        purchaseOrder.coupon = buildCoupon(purchaseOrder.coupon)
        purchaseOrder.freight = freightRepository.find(purchaseOrderId = purchaseOrder.id)

        return purchaseOrder
    }

    private fun buildCoupon(couponCode: CouponCode?): CouponCode? {
        return if (couponCode != null) {
            CouponCode(
                code = couponCode.code,
                reward = couponRepository.findByCoupon(couponCode),
                customFields = couponCode.customFields
            )
        } else null
    }

    override fun find(customer: Customer, status: String?, start: String?, end: String?): List<PurchaseOrder> {

        var purchaseOrders: List<PurchaseOrder>?
        var sql = """
                select * from $TABLE_NAME
                 where $CUSTOMER_COLUMN = ?
            """
        if (status != null) {
            sql = setSqlWithStatus(sql)
            if (start != null && end != null) {
                sql = setSqlWithDateRange(sql)
                purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), customer.id, status, start, end)
            } else {
                purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), customer.id, status)
            }
        } else if (start != null && end != null) {
            sql = setSqlWithDateRange(sql)
            purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), customer.id, start, end)

        } else {
            purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), customer.id)
        }

        purchaseOrders!!.forEach { buildFullPurchaseOrder(it) }

        return purchaseOrders
    }

    private fun setSqlWithStatus(sql: String): String {
        return "$sql\nand $STATUS_COLUMN = ?"
    }

    private fun setSqlWithDateRange(sql: String): String {
        return sql + "and $CREATED_COLUMN >= ?::timestamp" +
                "\nand $CREATED_COLUMN <= ?::timestamp"
    }

    override fun savePurchaseOrder(
        purchaseOrderId: PurchaseOrderId,
        type: PurchaseOrderType?,
        channel: Channel,
        callback: Callback?,
        customer: Customer?,
        version: Long
    ): Int {
        val sql = "insert into $TABLE_NAME (" +
                "$PURCHASE_ORDER_ID_COLUMN," +
                "$STATUS_COLUMN," +
                "$CREATED_COLUMN, " +
                "$VERSION_COLUMN," +
                "$PURCHASE_ORDER_TYPE_COLUMN," +
                "$CHANNEL_CREATE_COLUMN," +
                "$PURCHASE_ORDER_CALLBACK_URL, " +
                "$CUSTOMER_COLUMN, " +
                "$PURCHASE_ORDER_CALLBACK_HEADERS) values " +
                "(?, ?, now(), ?, ?, ?, ?, ?, ?::JSON)"
        return try {
            jdbcTemplate.update(
                sql,
                purchaseOrderId.value,
                PurchaseOrderStatus.OPENED.name,
                version,
                type?.name,
                channel.value,
                callback?.url,
                customer?.id,
                callback?.headers.objectToJson()
            )
        } catch (e: DuplicateKeyException) {
            log.warn("Fail to save Purchase order, purchase order duplicate for id: {}", purchaseOrderId.value, e)
            0
        }
    }

    override fun updateSegmentation(purchaseOrderId: PurchaseOrderId, segmentation: Segmentation, version: Long): Int {
        val sql = """update $TABLE_NAME
                set $SEGMENTATION_COLUMN = ?,
                $UPDATED_COLUMN = now() ,
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""
        return responseUpdate(
            rows = jdbcTemplate.update(
                sql, segmentation.query.objectToJson(), version, purchaseOrderId
                    .value, version
            ), purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateMgm(purchaseOrderId: PurchaseOrderId, mgm: Mgm?, version: Long): Int {
        val sql = """update $TABLE_NAME
                set $MGM_CODE_COLUMN = ?,
                $MGM_CUSTOM_FIELDS = ?::JSON,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql, mgm?.code, mgm?.customFieldsToStringJson(), version,
                purchaseOrderId.value, version
            ), purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateOnBoardingSale(
        purchaseOrderId: PurchaseOrderId,
        onBoardingSale: OnBoardingSale,
        version: Long
    ): Int {
        val sql = """update $TABLE_NAME
                set $ON_BOARDING_SALE_OFFER_ID = ?,
                $ON_BOARDING_SALE_CUSTOM_FIELDS = ?::JSON,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql, onBoardingSale.offer.value, onBoardingSale.customFields
                    .objectToJson(), version, purchaseOrderId.value, version
            ), purchaseOrderId = purchaseOrderId.value,
            version = version
        )
    }

    override fun updateCustomer(purchaseOrderId: PurchaseOrderId, customer: Customer, version: Long): Int {
        val sql = """update $TABLE_NAME
                set $CUSTOMER_COLUMN = ?,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        return responseUpdate(
            rows = jdbcTemplate.update(sql, customer.id, version, purchaseOrderId.value, version),
            purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateCoupon(purchaseOrderId: PurchaseOrderId, coupon: CouponCode, version: Long): Int {
        val sql = """update $TABLE_NAME
                set $COUPON_CODE_COLUMN = ?,
                $COUPON_CUSTOM_FIELDS = ?::JSON,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        val rows = responseUpdate(
            rows = jdbcTemplate.update(
                sql,
                coupon.code,
                coupon.customFieldsToStringJson(),
                version,
                purchaseOrderId.value,
                version
            ),
            purchaseOrderId = purchaseOrderId.value,
            version = version
        )

        val duplicatedCoupon = couponRepository.findByCoupon(coupon)

        if (duplicatedCoupon == null && coupon.reward != null) {
            couponRepository.saveDiscount(coupon)
        }

        return rows
    }

    override fun updatePayment(purchaseOrderId: PurchaseOrderId, paymentList: Payment, version: Long): Int {
        val rows = updatePaymentDescription(purchaseOrderId, paymentList.description, version)
        paymentRepository.savePayments(purchaseOrderId, paymentList)

        return rows
    }

    override fun updateFreight(purchaseOrderId: PurchaseOrderId, freight: Freight, version: Long): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId.value, version)
        return if (rows > 0)
            freightRepository.saveFreight(purchaseOrderId, freight)
        else
            rows
    }

    override fun updatePaymentDescription(
        purchaseOrderId: PurchaseOrderId,
        paymentDescription: Payment.Description?,
        version: Long
    ): Int {
        val sql = """update $TABLE_NAME
                set $PAYMENT_DESC = ?,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        return responseUpdate(
            rows = jdbcTemplate.update(sql, paymentDescription?.value, version, purchaseOrderId.value, version),
            purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateInstallationAttributes(
        purchaseOrderId: String,
        installationAttribute: InstallationAttribute,
        version: Long
    ): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId, version)
        return if (rows > 0)
            installationAttributesRepository.update(purchaseOrderId, installationAttribute)
        else
            rows
    }

    override fun deleteInstallationAttributes(
        purchaseOrderId: String,
        productTypeId: ProductTypeId,
        version: Long
    ): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId, version)
        return if (rows > 0)
            installationAttributesRepository.delete(purchaseOrderId, productTypeId)
        else
            rows
    }

    override fun deletePurchaseOrder(purchaseOrderId: PurchaseOrderId, version: Long): Int {
        val sql = """
            update $TABLE_NAME
            set $STATUS_COLUMN = ?,
            $UPDATED_COLUMN = now(),
            $VERSION_COLUMN = ?
            where $PURCHASE_ORDER_ID_COLUMN = ?
            and $VERSION_COLUMN = (? - 1)
            """

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql, PurchaseOrderStatus.DELETED.name, version,
                purchaseOrderId.value, version
            ), purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun addItem(purchaseOrderId: AggregateId, item: Item, version: Long): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId.value, version)
        return if (rows > 0)
            purchaseOrderItemRepository.addItem(purchaseOrderId, item)
        else
            rows
    }

    override fun removeItem(purchaseOrderId: AggregateId, itemId: Item.Id, version: Long): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId.value, version)
        return if (rows > 0)
            purchaseOrderItemRepository.removeItem(purchaseOrderId, itemId)
        else
            rows
    }

    override fun updateItem(purchaseOrderId: AggregateId, item: Item, version: Long): Int {
        val rows = updateVersionPurchaseOrder(purchaseOrderId.value, version)
        return if (rows > 0)
            purchaseOrderItemRepository.updateItem(purchaseOrderId = purchaseOrderId, item = item)
        else
            rows
    }

    override fun checkOutPurchaseOrder(
        purchaseOrderId: AggregateId,
        channel: Channel,
        version: Long,
        securityCodeInformed: List<SecurityCodeInformed>
    ): Int {
        val sql = """
            update $TABLE_NAME
            set $STATUS_COLUMN = ?,
            $UPDATED_COLUMN = now(),
            $VERSION_COLUMN = ?,
            $CHANNEL_CHECKOUT_COLUMN = ?
            where $PURCHASE_ORDER_ID_COLUMN = ?
            and $VERSION_COLUMN = (? - 1)
            """

        if (securityCodeInformed.isNotEmpty()) {
            paymentRepository.updatePaymentSecurityCode(purchaseOrderId, securityCodeInformed)
        }

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql,
                PurchaseOrderStatus.CHECKED_OUT.name,
                version,
                channel.value,
                purchaseOrderId.value,
                version
            ), purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateVersion(purchaseOrderId: AggregateId, version: Long): Int {
        return updateVersionPurchaseOrder(purchaseOrderId.value, version)
    }

    override fun updateProtocol(purchaseOrderId: AggregateId, protocol: Protocol, version: Long): Int {
        val sql = """
                update $TABLE_NAME
                set $PROTOCOL_COLUMN = ?,
                    $UPDATED_COLUMN = now(),
                    $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)
        """

        return responseUpdate(
            rows = jdbcTemplate.update(sql, protocol.value, version, purchaseOrderId.value, version),
            purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

    override fun updateSubscription(purchaseOrderId: AggregateId, subscription: Subscription, version: Long): Int {
        var sql = """
            update
                $TABLE_NAME
            set
                $SUBSCRIPTION_ID_COLUMN = ?,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
            where
                $PURCHASE_ORDER_ID_COLUMN = ?
            and
                $VERSION_COLUMN = (? - 1)
        """

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql,
                subscription.id,
                version,
                purchaseOrderId.value,
                version
            ),
            purchaseOrderId = purchaseOrderId.value,
            version = version
        )
    }

    override fun updatePurchaseOrderType(
        purchaseOrderId: AggregateId,
        purchaseOrderType: PurchaseOrderType?,
        version: Long
    ): Int {
        var sql = """
            update
                $TABLE_NAME
            set
                $PURCHASE_ORDER_TYPE_COLUMN = ?,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
            where
                $PURCHASE_ORDER_ID_COLUMN = ?
            and
                $VERSION_COLUMN = (? - 1)
        """

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql,
                purchaseOrderType?.name,
                version,
                purchaseOrderId.value,
                version
            ),
            purchaseOrderId = purchaseOrderId.value,
            version = version
        )
    }

    override fun findByProtocol(protocol: Protocol): PurchaseOrder? {
        val sql = "select * from $TABLE_NAME where $PROTOCOL_COLUMN = ?"
        var purchaseOrders = jdbcTemplate.query(sql, PurchaseOrderMapper(), protocol.value)

        return when (purchaseOrders.size) {
            0 -> null
            1 -> {
                val purchaseOrder = purchaseOrders[0]
                purchaseOrder.installationAttributes =
                        installationAttributesRepository.findAll(purchaseOrder.id.value) as
                                HashMap<ProductTypeId, InstallationAttribute>
                purchaseOrder.items = purchaseOrderItemRepository.findByPurchaseOrderId(
                    PurchaseOrderId(purchaseOrder.id.value)
                ).toMutableSet()
                purchaseOrder.customerOrder = customerOrderRepository.findOne(PurchaseOrderId(purchaseOrder.id.value))

                purchaseOrder
            }
            else -> throw IncorrectResultSizeDataAccessException(1, purchaseOrders.size)
        }
    }

    private fun responseUpdate(rows: Int, purchaseOrderId: String, version: Long): Int {
        if (rows == 0) {
            val versionSaved = findVersion(purchaseOrderId)
            if (versionSaved < version) {
                log.warn(
                    "Generated exception for update of purchaseOrderId:{}, version received: {} is larger that " +
                            "the version saved: {}", purchaseOrderId, version, versionSaved
                )
                throw PurchaseOrderVersionException()
            } else {
                log.warn(
                    "Update ignored for the purchaseOrderId:{}, version received: {}, version saved: {}",
                    purchaseOrderId, version, versionSaved
                )
            }
        }
        return rows
    }

    private fun findVersion(purchaseOrderId: String): Int {
        val sql = "select $VERSION_COLUMN from $TABLE_NAME where $PURCHASE_ORDER_ID_COLUMN = ?"
        return jdbcTemplate.queryForObject(sql, Int::class.java, purchaseOrderId)
    }

    private fun updateVersionPurchaseOrder(purchaseOrderId: String, version: Long): Int {
        val sql = """
            update $TABLE_NAME
            set $VERSION_COLUMN = ?
            where $PURCHASE_ORDER_ID_COLUMN = ?
            and $VERSION_COLUMN = (? - 1)
            """
        return responseUpdate(
            rows = jdbcTemplate.update(sql, version, purchaseOrderId, version),
            purchaseOrderId = purchaseOrderId,
            version = version
        )
    }

    override fun updateStatus(purchaseOrderId: AggregateId, status: PurchaseOrderStatus, version: Long): Int {
        var sql = """
            update $TABLE_NAME
               set $STATUS_COLUMN = ?,
                   $UPDATED_COLUMN = now(),
                   $VERSION_COLUMN = ?
             where $PURCHASE_ORDER_ID_COLUMN = ?
               and $VERSION_COLUMN = (? - 1)
        """

        return responseUpdate(
            rows = jdbcTemplate.update(sql, status.name, version, purchaseOrderId.value, version),
            purchaseOrderId = purchaseOrderId.value,
            version = version
        )
    }

    override fun updatePurchaseOrderReason(purchaseOrderId: AggregateId, reason: Reason?, version: Long): Int {
        var sql = """
            update
                $TABLE_NAME
            set
                $PURCHASE_ORDER_REASON = ?::JSON,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
            where
                $PURCHASE_ORDER_ID_COLUMN = ?
            and
                $VERSION_COLUMN = (? - 1)
        """

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql,
                reason.objectToJson(),
                version,
                purchaseOrderId.value,
                version
            ),
            purchaseOrderId = purchaseOrderId.value,
            version = version
        )

    }

    override fun updateSalesForce(purchaseOrderId: PurchaseOrderId, salesForce: SalesForce?, version: Long): Int {
        val sql = """update $TABLE_NAME
                set $SALES_FORCE_ID_COLUMN = ?,
                $SALES_FORCE_NAME_COLUMN = ?,
                $UPDATED_COLUMN = now(),
                $VERSION_COLUMN = ?
                where $PURCHASE_ORDER_ID_COLUMN = ?
                and $VERSION_COLUMN = (? - 1)"""

        return responseUpdate(
            rows = jdbcTemplate.update(
                sql, salesForce?.id, salesForce?.name, version,
                purchaseOrderId.value, version
            ), purchaseOrderId = purchaseOrderId.value, version = version
        )
    }

}
