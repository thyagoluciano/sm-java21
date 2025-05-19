package br.com.zup.realwave.sales.manager.command.application.controller.exception

import br.com.zup.realwave.sales.manager.infrastructure.common.exception.ErrorMessageResponse
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.SalesManagerCheckoutException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.SalesManagerCheckoutUnknownException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.SalesManagerRPCException
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObjectOrNull
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.integration.feign.apis.CustomerOrderRequestBuildException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@ControllerAdvice
@Order(Int.MAX_VALUE - 1)
class CheckoutExceptionHandler(val messageSource: MessageSource) : WebMvcConfigurerAdapter() {

    @ExceptionHandler(SalesManagerCheckoutException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptions(e: SalesManagerCheckoutException): String {
            return ErrorMessageResponse(
            code = e.errorCode.code,
            message = messageSource.getMessage(
                PurchaseOrderErrorCode.PURCHASE_ORDER_CHECKOUT_ERROR.key,
                null,
                LocaleContextHolder.getLocale()
            ),
            cause = messageErrorResponse(e),
            detailMessage = validationErrorResponse(e)
        ).objectToJson()
    }

    @ExceptionHandler(SalesManagerCheckoutUnknownException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun exceptions(e: SalesManagerCheckoutUnknownException): String {
        return ErrorMessageResponse(
            code = e.errorCode.code,
            message = messageSource.getMessage(
                PurchaseOrderErrorCode.PURCHASE_ORDER_CHECKOUT_ERROR.key,
                null,
                LocaleContextHolder.getLocale()
            ),
            cause = messageErrorResponse(e),
            detailMessage = validationErrorResponse(e)
        ).objectToJson()
    }

    @ExceptionHandler(CustomerOrderRequestBuildException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun exceptions(e: CustomerOrderRequestBuildException): String {
        return ErrorMessageResponse(
            code = e.errorCode.code,
            message = messageSource.getMessage(
                PurchaseOrderErrorCode.CATALOG_OFFER_NOT_RETURNED_BY_OFFER_DETAILS.key,
                arrayOf(e.offerId),
                LocaleContextHolder.getLocale()
            )
        ).objectToJson()
    }

    private fun validationErrorResponse(e: SalesManagerRPCException): String? =
        if (messageErrorResponse(e) == null) e.response else null

    private fun messageErrorResponse(e: SalesManagerRPCException): ErrorMessageResponse? =
        e.response.jsonToObjectOrNull(ErrorMessageResponse::class.java)

}
