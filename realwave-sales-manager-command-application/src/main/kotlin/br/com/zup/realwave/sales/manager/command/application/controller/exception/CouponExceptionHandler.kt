package br.com.zup.realwave.sales.manager.command.application.controller.exception

import br.com.zup.realwave.sales.manager.infrastructure.common.exception.ErrorMessageResponse
import br.com.zup.realwave.sales.manager.infrastructure.jsonToObjectOrNull
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CouponErrorDecoder
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
@Order(Int.MAX_VALUE)
class CouponExceptionHandler(val messageSource: MessageSource) : WebMvcConfigurerAdapter() {

    @ExceptionHandler(CouponErrorDecoder.CouponValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptions(e: CouponErrorDecoder.CouponValidationException): String {
        return ErrorMessageResponse(
            code = e.errorCode.code,
            message = messageSource.getMessage(e.errorCode.key, null, LocaleContextHolder.getLocale()),
            cause = messageErrorResponse(e),
            detailMessage = validationErrorResponse(e)
        ).objectToJson()
    }

    private fun validationErrorResponse(e: CouponErrorDecoder.CouponValidationException): String? =
        if (messageErrorResponse(e) == null) e.response
        else null

    private fun messageErrorResponse(e: CouponErrorDecoder.CouponValidationException): ErrorMessageResponse? =
        e.response.jsonToObjectOrNull(ErrorMessageResponse::class.java)

}
