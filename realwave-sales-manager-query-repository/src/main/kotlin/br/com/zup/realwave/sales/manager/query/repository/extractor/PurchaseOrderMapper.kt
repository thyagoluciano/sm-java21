package br.com.zup.realwave.sales.manager.query.repository.extractor

import br.com.zup.realwave.sales.manager.domain.Callback
import br.com.zup.realwave.sales.manager.domain.CatalogOfferId
import br.com.zup.realwave.sales.manager.domain.Channel
import br.com.zup.realwave.sales.manager.domain.CouponCode
import br.com.zup.realwave.sales.manager.domain.Customer
import br.com.zup.realwave.sales.manager.domain.Mgm
import br.com.zup.realwave.sales.manager.domain.OnBoardingSale
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.PurchaseOrder
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderId
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderStatus
import br.com.zup.realwave.sales.manager.domain.PurchaseOrderType
import br.com.zup.realwave.sales.manager.domain.Reason
import br.com.zup.realwave.sales.manager.domain.SalesForce
import br.com.zup.realwave.sales.manager.domain.Segmentation
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObject
import br.com.zup.realwave.sales.manager.query.repository.JdbcPurchaseOrderRepository
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * Created by Danilo Paiva on 26/05/17
 */
class PurchaseOrderMapper : RowMapper<PurchaseOrder> {

    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call `next()` on
     * the ResultSet; it is only supposed to map values of the current row.
     * @param rs the ResultSet to map (pre-initialized for the current row)
     * *
     * @param rowNum the number of the current row
     * *
     * @return the result object for the current row
     * *
     * @throws SQLException if a SQLException is encountered getting
     * * column values (that is, there's no need to catch SQLException)
     */
    override fun mapRow(rs: ResultSet, rowNum: Int): PurchaseOrder =
        PurchaseOrder()
            .mapPurchaseOrder(rs)
            .mapCoupon(rs)
            .mapMgm(rs)
            .mapSegmentation(rs)
            .mapOnBoardingSales(rs)
            .mapSalesForce(rs)
            .mapCallback(rs)
            .mapCustomer(rs)
            .mapSubscription(rs)
            .mapChannel(rs)
            .mapPayment(rs)
            .mapProtocol(rs)

    private fun PurchaseOrder.mapPurchaseOrder(rs: ResultSet) = this.apply {
        id = PurchaseOrderId(rs.getString(JdbcPurchaseOrderRepository.PURCHASE_ORDER_ID_COLUMN))
        type = PurchaseOrderType.valueOf(rs.getString(JdbcPurchaseOrderRepository.PURCHASE_ORDER_TYPE_COLUMN))
        status = PurchaseOrderStatus.valueOf(rs.getString(JdbcPurchaseOrderRepository.STATUS_COLUMN))
        createdAt = rs.getString(JdbcPurchaseOrderRepository.CREATED_COLUMN)
        updatedAt = rs.getString(JdbcPurchaseOrderRepository.UPDATED_COLUMN)
        reason = rs.getString(JdbcPurchaseOrderRepository.PURCHASE_ORDER_REASON)?.jsonToObject(Reason::class.java)
    }

    private fun PurchaseOrder.mapCoupon(rs: ResultSet) = this.apply {
        val couponCode = rs.getString(JdbcPurchaseOrderRepository.COUPON_CODE_COLUMN)
        val couponCustomFields = rs.getString(JdbcPurchaseOrderRepository.COUPON_CUSTOM_FIELDS)
        if (StringUtils.isNotEmpty(couponCode)) {
            var customFieldsNode: JsonNode? = null
            if (StringUtils.isNotEmpty(couponCustomFields)) {
                customFieldsNode = couponCustomFields.jsonToObject(JsonNode::class.java)
            }
            coupon = CouponCode(code = couponCode, customFields = customFieldsNode)
        }
    }

    private fun PurchaseOrder.mapMgm(rs: ResultSet) = this.apply {
        val mgmCode = rs.getString(JdbcPurchaseOrderRepository.MGM_CODE_COLUMN)
        val mgmCustomFields = rs.getString(JdbcPurchaseOrderRepository.MGM_CUSTOM_FIELDS)
        if (StringUtils.isNotEmpty(mgmCode)) {
            var customFieldsNode: JsonNode? = null
            if (StringUtils.isNotEmpty(mgmCustomFields)) {
                customFieldsNode = mgmCustomFields.jsonToObject(JsonNode::class.java)
            }
            mgm = Mgm(code = mgmCode, customFields = customFieldsNode)
        }
    }

    private fun PurchaseOrder.mapSegmentation(rs: ResultSet) = this.apply {
        val segmentationQuery = rs.getString(JdbcPurchaseOrderRepository.SEGMENTATION_COLUMN)
        if (StringUtils.isNotEmpty(segmentationQuery)) {
            segmentation = Segmentation(segmentationQuery.jsonToObject(JsonNode::class.java))
        }
    }

    private fun PurchaseOrder.mapOnBoardingSales(rs: ResultSet) = this.apply {
        val onBoardingSaleOfferId = rs.getString(JdbcPurchaseOrderRepository.ON_BOARDING_SALE_OFFER_ID)
        val onBoardingSaleCustomFields = rs.getString(JdbcPurchaseOrderRepository.ON_BOARDING_SALE_CUSTOM_FIELDS)
        if (StringUtils.isNotEmpty(onBoardingSaleOfferId)) {
            var customFieldsNode: JsonNode? = null
            if (StringUtils.isNotEmpty(onBoardingSaleCustomFields)) {
                customFieldsNode = onBoardingSaleCustomFields.jsonToObject(JsonNode::class.java)
            }
            val offerId = CatalogOfferId(onBoardingSaleOfferId)
            onBoardingSale = OnBoardingSale(offer = offerId, customFields = customFieldsNode)
        }
    }

    private fun PurchaseOrder.mapSalesForce(rs: ResultSet) = this.apply {
        val salesForceId = rs.getString(JdbcPurchaseOrderRepository.SALES_FORCE_ID_COLUMN)
        val salesForceName = rs.getString(JdbcPurchaseOrderRepository.SALES_FORCE_NAME_COLUMN)
        if (StringUtils.isNotEmpty(salesForceId) && StringUtils.isNotEmpty(salesForceName)) {
            salesForce = SalesForce(id = salesForceId, name = salesForceName)
        }
    }

    private fun PurchaseOrder.mapCallback(rs: ResultSet) = this.apply {
        var callbackUrlValue = rs.getString(JdbcPurchaseOrderRepository.PURCHASE_ORDER_CALLBACK_URL)
        var callbackHeadersValue = rs.getString(JdbcPurchaseOrderRepository.PURCHASE_ORDER_CALLBACK_HEADERS)
        if (StringUtils.isNotEmpty(callbackUrlValue)) {
            var customFieldsNode: JsonNode? = null
            if (StringUtils.isNotEmpty(callbackHeadersValue)) {
                customFieldsNode = callbackHeadersValue.jsonToObject(JsonNode::class.java)
            }
            callback = Callback(callbackUrlValue, customFieldsNode)
        }
    }

    private fun PurchaseOrder.mapCustomer(rs: ResultSet) = this.apply {
        val customerId = rs.getString(JdbcPurchaseOrderRepository.CUSTOMER_COLUMN)
        if (null != customerId) customer = Customer(customerId)
    }

    private fun PurchaseOrder.mapChannel(rs: ResultSet) = this.apply {
        this.channelCreate = Channel(rs.getString(JdbcPurchaseOrderRepository.CHANNEL_CREATE_COLUMN))
        this.channelCheckout = Channel(rs.getString(JdbcPurchaseOrderRepository.CHANNEL_CHECKOUT_COLUMN))
    }

    private fun PurchaseOrder.mapSubscription(rs: ResultSet) = this.apply {
        subscriptionId = rs.getString(JdbcPurchaseOrderRepository.SUBSCRIPTION_ID_COLUMN)
    }

    private fun PurchaseOrder.mapPayment(rs: ResultSet) = this.apply {
        val paymentDescription = rs.getString(JdbcPurchaseOrderRepository.PAYMENT_DESC)
        payment = if (StringUtils.isNotEmpty(paymentDescription)) {
            Payment(description = Payment.Description(paymentDescription))
        } else {
            Payment()
        }
    }

    private fun PurchaseOrder.mapProtocol(rs: ResultSet) = this.apply {
        protocol = rs.getString(JdbcPurchaseOrderRepository.PROTOCOL_COLUMN)
    }

}
