package br.com.zup.realwave.sales.manager.integration.feign.apis

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.infrastructure.common.exception.MemberGetMemberErrorCode
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.logging.log4j.LogManager
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.lang.Exception

/**
 * Created by branquinho on 08/08/17.
 */
@Headers("Accept: application/json")
interface MemberGetMemberApiService {

    @RequestLine("GET /v2/member/{memberGetMemberCode}/validate")
    fun validate(@Param("memberGetMemberCode") name: String)

}

@Component
class MemberGetMemberErrorDecoder(val counterService: CounterService) : ErrorDecoder {

    private val log = LogManager.getLogger(this.javaClass)

    override fun decode(methodKey: String, response: Response): Exception =
        when {
            response.status() == HttpStatus.NOT_FOUND.value() ->
                BusinessException.of(
                    MemberGetMemberErrorCode.MEMBER_GET_MEMBER_VALIDATION,
                    "not found"
                )
            response.status() == HttpStatus.BAD_REQUEST.value() ->
                BusinessException.of(
                    MemberGetMemberErrorCode.MEMBER_GET_MEMBER_VALIDATION,
                    String(response.body().asInputStream().readBytes())
                )
            else ->
                BusinessException.of(
                    MemberGetMemberErrorCode.MEMBER_GET_MEMBER_INTEGRATION_ERROR
                ).also {
                    log.error("Response: {}", response)
                }
        }.also {
            counterService.increment("status.${response.status()}.$methodKey")
        }

}
