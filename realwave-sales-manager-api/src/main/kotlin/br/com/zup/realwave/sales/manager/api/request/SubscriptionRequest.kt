package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank

data class SubscriptionRequest(@field:NotBlank val id: String)
