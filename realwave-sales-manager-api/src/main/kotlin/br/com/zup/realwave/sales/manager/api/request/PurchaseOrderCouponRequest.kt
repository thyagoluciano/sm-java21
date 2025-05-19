package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank

data class PurchaseOrderCouponRequest(
    @field:NotBlank val couponCode: String?,
    @field:NotBlank val productId: String?,
    @field:NotBlank val customerId: String?,
    val callback: CallbackRequest? = null
)
