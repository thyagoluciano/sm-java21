package br.com.zup.realwave.sales.manager.domain.service

interface CustomerInfoService {

    fun validateCustomer(customer: String)

    fun validateProduct(customerId: String, productId: String)

}
