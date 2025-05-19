package br.com.zup.realwave.sales.manager.command.application.filter

import br.com.zup.realwave.common.context.utils.RealwaveContextConstants
import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.common.exception.handler.to.ErrorMessageResponseGenerator
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.ApplicationErrorCode
import br.com.zup.realwave.sales.manager.infrastructure.objectToJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TenantValidationFilter @Autowired constructor(
    val generator: ErrorMessageResponseGenerator
) : GenericFilterBean() {

    @Value("#{'\${paths.to.validate.headers}'.split(',')}")
    lateinit var pathsToValidateHeaders: MutableList<String>

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        return when {

            request !is HttpServletRequest || response !is HttpServletResponse ->
                throw IllegalArgumentException("Unhandled type.")

            !shouldValidateRequest(request.requestURI) -> {
                chain!!.doFilter(request, response)
            }

            request.getHeader(RealwaveContextConstants.ORGANIZATION_SLUG_HEADER).isNullOrBlank() -> {
                val content = generator.buildWithBusinessException(
                    BusinessException.of(
                        RealwaveContextConstants.ORGANIZATION_SLUG_HEADER,
                        ApplicationErrorCode.ORGANIZATION_NOT_RECEIVED
                    )
                )
                fillErrorResponse(content, response)
            }

            request.getHeader(RealwaveContextConstants.APPLICATION_ID_HEADER).isNullOrBlank() -> {
                val content = generator.buildWithBusinessException(
                    BusinessException.of(
                        RealwaveContextConstants.APPLICATION_ID_HEADER,
                        ApplicationErrorCode.APPLICATION_NOT_RECEIVED
                    )
                )
                fillErrorResponse(content, response)
            }
            else -> chain!!.doFilter(request, response)
        }
    }

    private fun shouldValidateRequest(requestURI: String): Boolean =
        pathsToValidateHeaders.any {
            requestURI.contains(it)
        }

    private fun fillErrorResponse(content: Any, response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_BAD_REQUEST
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)

        response.writer.print(content.objectToJson())
        response.writer.flush()
    }

}
