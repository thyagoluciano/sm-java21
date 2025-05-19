package br.com.zup.realwave.sales.manager.domain

import br.com.zup.eventsourcing.core.AggregateRoot
import br.com.zup.eventsourcing.core.Event
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderApplicableEvent
import br.com.zup.realwave.sales.manager.domain.event.PurchaseOrderCreated
import org.apache.logging.log4j.LogManager
import java.time.format.DateTimeFormatter

/**
 * Created by Danilo Paiva on 26/05/17
 */
class PurchaseOrder() : AggregateRoot() {

    internal val log = LogManager.getLogger(this.javaClass)

    var segmentation: Segmentation? = null
    var onBoardingSale: OnBoardingSale? = null
    var mgm: Mgm? = null
    var customer: Customer? = null
    var coupon: CouponCode? = null
    var payment: Payment = Payment()
    var freight: Freight? = null
    var customerOrder: CustomerOrder? = null
    var installationAttributes = HashMap<ProductTypeId, InstallationAttribute>()
    var items: MutableSet<Item> = HashSet()
    var createdAt: String? = null
    var updatedAt: String? = null
    var protocol: String? = null
    var type: PurchaseOrderType? = null
    var subscriptionId: String? = null
    var channelCreate: Channel? = null
    var channelCheckout: Channel? = null
    var callback: Callback? = null
    var reason: Reason? = null
    var securityCodeInformed: List<SecurityCodeInformed>? = emptyList()
    lateinit var status: PurchaseOrderStatus
    var salesForce: SalesForce? = null

    fun idAsString(): String = id.value

    constructor(
        aggregateId: PurchaseOrderId,
        purchaseOrderType: PurchaseOrderType?,
        callback: Callback? = null,
        customer: Customer? = null
    ) : this() {
        applyChange(
            PurchaseOrderCreated(
                aggregateId = aggregateId,
                purchaseOrderType = purchaseOrderType,
                customer = customer,
                callback = callback
            )
        )
    }

    override fun applyEvent(event: Event) {
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
        this.event = event

        if (event::class == PurchaseOrderCreated::class)
            this.createdAt = this.createdAt ?: event.eventPersistedDate?.value?.format(formatter)

        if (event is PurchaseOrderApplicableEvent) event.apply(this)
        else throw InvalidEvent(
            "Invalid event type ${event.javaClass}. " +
                    "It must be subtype of ${PurchaseOrderApplicableEvent::class.java}"
        )
    }

    override fun equals(other: Any?): Boolean =
        other != null
                && other.javaClass == javaClass
                && equals(this, other as PurchaseOrder)

    override fun hashCode(): Int = id.hashCode()

    class InvalidEvent(msg: String) : IllegalArgumentException(msg)

}
