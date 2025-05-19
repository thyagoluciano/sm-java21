package br.com.zup.realwave.sales.manager.domain

/**
 * Created by Danilo Paiva on 05/06/17
 */
enum class PurchaseOrderStatus {
    OPENED,
    CHECKED_OUT,
    COMPLETED,
    FAILED,
    CANCELED,
    DELETED;
}
