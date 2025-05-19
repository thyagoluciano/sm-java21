package br.com.zup.realwave.sales.manager.integration.feign.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomerSearchResponse(val id: String, val status: String?)
