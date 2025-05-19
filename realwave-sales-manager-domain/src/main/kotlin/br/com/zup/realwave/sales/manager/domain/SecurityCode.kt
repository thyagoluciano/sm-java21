package br.com.zup.realwave.sales.manager.domain

data class SecurityCode(val methodId: String, val securityCode: String)

data class SecurityCodeInformed(val methodId: String, val securityCodeInformed: Boolean)
