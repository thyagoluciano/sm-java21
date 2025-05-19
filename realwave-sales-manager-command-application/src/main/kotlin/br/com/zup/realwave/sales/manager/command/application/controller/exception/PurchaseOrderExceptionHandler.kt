package br.com.zup.realwave.sales.manager.command.application.controller.exception

import br.com.zup.realwave.sales.manager.infrastructure.common.exception.ErrorMessageResponse
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.PurchaseOrderValidationMethodsException
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
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
@Order(Int.MAX_VALUE - 3)
class PurchaseOrderExceptionHandler(val messageSource: MessageSource) : WebMvcConfigurerAdapter() {

    @ExceptionHandler(PurchaseOrderValidationMethodsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptions(e: PurchaseOrderValidationMethodsException): String {
        return ErrorMessageResponse(
                code = e.errorCode.code,
                message = messageSource.getMessage(e.errorCode.key, null, LocaleContextHolder.getLocale()),
                detailMessage = e.methods.joinToString(prefix = "[", postfix = "]" ){it.toString()}
        ).objectToJson()
    }

}
