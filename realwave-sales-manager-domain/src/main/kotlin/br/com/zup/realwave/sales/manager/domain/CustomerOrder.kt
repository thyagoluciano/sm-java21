package br.com.zup.realwave.sales.manager.domain

/**
 * Created by branquinho on 11/07/17.
 */
data class CustomerOrder(val customerOrderId: String?, val status: String?, val steps: List<Step>?, val boleto: Boleto? = null)
