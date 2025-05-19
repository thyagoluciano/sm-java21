package br.com.zup.realwave.sales.manager.integration.feign.services

import br.com.zup.realwave.sales.manager.domain.service.MemberGetMemberService
import br.com.zup.realwave.sales.manager.integration.feign.apis.MemberGetMemberApiService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by branquinho on 09/08/17.
 */
@Service
class MemberGetMemberServiceClientService @Autowired constructor(
    val memberGetMemberApiService: MemberGetMemberApiService
) : MemberGetMemberService {

    private val log = LogManager.getLogger(this.javaClass)

    override fun validate(memberGetMemberCode: String?) {
        if (memberGetMemberCode != null) {
            log.info("Begin validate member code in mgm api")
            memberGetMemberApiService.validate(memberGetMemberCode)
            log.info("End validate member code in mgm api")
        }
    }

}
