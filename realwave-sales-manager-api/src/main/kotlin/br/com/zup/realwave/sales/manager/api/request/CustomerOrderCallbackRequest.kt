package br.com.zup.realwave.sales.manager.api.request

import org.hibernate.validator.constraints.NotBlank

data class CustomerOrderCallbackRequest(
    @field:[NotBlank] val id: String?,
    @field:[NotBlank] val externalId: String?,
    @field:[NotBlank] val status: String?,
    val steps: List<Step>?,
    val reason: Reason?
)

data class Step(
    val step: String?,
    val status: String?,
    val startedAt: String?,
    val endedAt: String?,
    val processed: Int?,
    val total: Int?
)

data class Reason(val code: String?, val description: String?)
