package br.com.zup.realwave.sales.manager.domain

/**
 * Created by Danilo Paiva on 02/06/17
 */
data class Customer(val id: String)

data class Status(val name: String) {
    companion object {
        const val ACTIVE = "ACTIVE"
    }
}
