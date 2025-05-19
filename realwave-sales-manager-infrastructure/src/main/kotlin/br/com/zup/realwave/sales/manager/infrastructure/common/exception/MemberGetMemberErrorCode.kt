package br.com.zup.realwave.sales.manager.infrastructure.common.exception

import br.com.zup.realwave.common.exception.handler.to.ErrorCode

/**
 * Created by branquinho on 10/08/17.
 */
data class MemberGetMemberErrorCode private constructor(val errorCode: String, val errorKey: String) :
    ErrorCode(errorCode, errorKey) {
    companion object {
        val MEMBER_GET_MEMBER_INTEGRATION_ERROR =
            MemberGetMemberErrorCode("MEMBER_GET_MEMBER_INTEGRATION_ERROR", "member.get.member.integration.error")
        val MEMBER_GET_MEMBER_VALIDATION: MemberGetMemberErrorCode =
            MemberGetMemberErrorCode("MGM", "member.get.member.integration.validation.error")
    }
}
