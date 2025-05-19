package br.com.zup.realwave.sales.manager.infrastructure.common.exception

import br.com.zup.realwave.common.exception.handler.to.ErrorCode

/**
 * Created by Kings on 7/6/17.
 */
class ApplicationErrorCode private constructor(errorCode: String, errorKey: String) : ErrorCode(errorCode, errorKey) {
    companion object {
        val ORGANIZATION_NOT_RECEIVED: ErrorCode =
            ApplicationErrorCode("ORGANIZATION_NOT_RECEIVED", "organization.slug.not.received")
        val APPLICATION_NOT_RECEIVED: ErrorCode =
            ApplicationErrorCode("APPLICATION_NOT_RECEIVED", "application.slug.not.received")
    }
}
