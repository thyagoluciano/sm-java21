package br.com.zup.realwave.sales.manager.command.application.controller.exception

import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import br.com.zup.realwave.sales.manager.integration.feign.decoder.CatalogSearchErrorDecoder
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
@Order(Int.MAX_VALUE - 2)
class CatalogManagerSearchExceptionHandler(val messageSource: MessageSource) : WebMvcConfigurerAdapter() {

    @ExceptionHandler(CatalogSearchErrorDecoder.CatalogSearchValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun exceptions(e: CatalogSearchErrorDecoder.CatalogSearchValidationException): String {
        return CatalogSearchErrorDecoder.CatalogSearchManagerErrorMessageResponse(
            code = e.errorCode.code,
            message = messageSource.getMessage(e.errorCode.key, null, LocaleContextHolder.getLocale()),
            cause = e.response
        ).objectToJson()
    }

}
