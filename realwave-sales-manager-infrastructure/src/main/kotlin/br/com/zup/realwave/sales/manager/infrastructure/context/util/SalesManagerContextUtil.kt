package br.com.zup.realwave.sales.manager.infrastructure.context.util

import br.com.zup.realwave.common.context.RealwaveContext
import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.common.context.utils.RealwaveContextConstants

/**
 * Created by marcosgm on 06/07/17
 */
class SalesManagerContextUtil {
    companion object {
        fun loadContext(map: MutableMap<String, Any>): RealwaveContext? {
            val context = RealwaveContextHolder.getContext()
            context.globalTrackingId = map.get(key = RealwaveContextConstants.TRACKING_ID_HEADER) as String?
            context.contextTrackingId = map.get(key = RealwaveContextConstants.TRACKING_CONTEXT_HEADER) as String?
            context.application = map.get(key = RealwaveContextConstants.APPLICATION_ID_HEADER) as String
            context.organization = map.get(key = RealwaveContextConstants.ORGANIZATION_SLUG_HEADER) as String
            context.channel = map.get(key = RealwaveContextConstants.CHANNEL_CONTEXT_HEADER) as String?
            return context
        }
    }
}
