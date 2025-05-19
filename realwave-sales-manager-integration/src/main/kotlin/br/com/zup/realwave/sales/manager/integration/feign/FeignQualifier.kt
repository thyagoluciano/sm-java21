package br.com.zup.realwave.sales.manager.integration.feign

/**
 * Created by Danilo Paiva on 06/06/17
 */
class FeignQualifier {

    companion object {
        const val CATALOG_MANAGER_API: String = "catalog-manager-api"
        const val EVENT_STORE_API: String = "event-store-api"
        const val CUSTOMER_ORDER_MANAGER_API: String = "customer-order-manager-api"
    }
}
