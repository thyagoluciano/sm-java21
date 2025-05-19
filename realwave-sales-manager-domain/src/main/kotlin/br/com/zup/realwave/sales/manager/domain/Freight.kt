package br.com.zup.realwave.sales.manager.domain


data class Freight(
    val address: Address,
    val price: Price,
    val type: Type,
    val deliveryTotalTime: DeliveryTotalTime
) {
    data class Address(
        val city: String,
        val complement: String? = null,
        val country: String,
        val district: String,
        val name: String,
        val state: String,
        val street: String,
        val zipCode: String,
        val number: String
    )

    data class Type(val value: String)

    data class DeliveryTotalTime(val value: Int)
}
