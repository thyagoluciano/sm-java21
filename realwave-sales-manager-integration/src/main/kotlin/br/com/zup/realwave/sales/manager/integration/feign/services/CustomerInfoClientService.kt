package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.domain.Status
import br.com.zup.realwave.sales.manager.domain.service.CustomerInfoService
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerApiService
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerSearchApiService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerInfoClientService @Autowired constructor(
    var customerInfoApi: CustomerSearchApiService,
    var customerApi: CustomerApiService
) : CustomerInfoService {

    private val log = LogManager.getLogger(this.javaClass)

    override fun validateProduct(customerId: String, productId: String) {
        try {
            log.info("Begin find customer id and product id on customer search api")
            val poductInfoResponse = customerInfoApi.findByCustomerIdAndProductId(customerId, productId)
            log.info("End find customer id and product id on customer search api")
            if (poductInfoResponse.status != Status.ACTIVE) {
                throw CustomerInactiveException()
            }
        } catch (e: BusinessException) {
            throw BusinessException.of("Product", PurchaseOrderErrorCode.PRODUCT_NOT_FOUND)
        } catch (e: CustomerInactiveException) {
            throw BusinessException.of("Prouct", PurchaseOrderErrorCode.PRODUCT_INACTIVE)
        }
    }

    override fun validateCustomer(customer: String) {
        try {
            log.info("Begin search by customer id {} in customer search", customer)
            var customerInfoResponse = customerInfoApi.apiFindById(customer)
            log.info("End search by customer id {} in customer search - status {}", customer, customerInfoResponse.status)

            try {
                if (customerInfoResponse.status == null){
                    log.info("[BUG_462] Customer {}: status not found on customer search", customer)
                    customerInfoResponse = customerApi.findCustomerById(customer);
                    log.info("[BUG_462] Getting in customer manager - customer {}: status {}",customer, customerInfoResponse.status)
                }
            }catch (e: Exception){
                log.warn("[BUG_462] Error validating customer on customer manager {}", customer)
            }

            if (customerInfoResponse.status != Status.ACTIVE) {
                throw CustomerInactiveException()
            }
        } catch (e: BusinessException) {
            log.warn("Error validating customer {}: {}", customer, e.message)
            throw BusinessException.of("customer", PurchaseOrderErrorCode.CUSTOMER_NOT_FOUND, customer)
        } catch (e: CustomerInactiveException) {
            log.warn("Error validating customer {}: {}", customer, e.message)
            throw BusinessException.of("customer", PurchaseOrderErrorCode.CUSTOMER_INACTIVE, customer)
        }
    }

}

class CustomerInactiveException : RuntimeException()
