package br.com.zup.realwave.sales.manager.domain.repository

import br.com.zup.eventsourcing.core.AggregateId
import br.com.zup.realwave.sales.manager.domain.Payment
import br.com.zup.realwave.sales.manager.domain.SecurityCodeInformed

interface PaymentRepository {

    fun findAll(purchaseOrderId: AggregateId): List<Payment.PaymentMethod>

    fun savePayments(purchaseOrderId: AggregateId, paymentList: Payment): Int

    fun updatePaymentSecurityCode(purchaseOrderId: AggregateId, secutiryCodeInformed: List<SecurityCodeInformed>?)

    fun updateMethodId(purchaseOrderId: AggregateId, methodId: String): Int
}
