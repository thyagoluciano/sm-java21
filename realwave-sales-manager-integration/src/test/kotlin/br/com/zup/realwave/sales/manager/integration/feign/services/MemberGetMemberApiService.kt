package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.common.exception.handler.exception.BusinessException
import br.com.zup.realwave.sales.manager.integration.config.ItegrationBaseTest
import br.com.zup.realwave.sales.manager.integration.feign.apis.MemberGetMemberApiService
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull


class MemberGetMemberTest : ItegrationBaseTest() {

    @Autowired
    private lateinit var memberGetMember: MemberGetMemberApiService

    @Test
    fun codesShouldBeValid() {
        val response = memberGetMember.validate("ABCDEF")
        assertNotNull(response)
    }

    @Test
    fun whenIntegrationErrorTheExceptionShouldNotBeEmpty() {
        try {
            memberGetMember.validate("ABC")
        } catch (e: BusinessException) {
            assert(e.message!!.isNotEmpty())
        }
    }

}
