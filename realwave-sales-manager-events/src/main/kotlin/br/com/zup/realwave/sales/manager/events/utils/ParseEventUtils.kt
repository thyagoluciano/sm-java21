package br.com.zup.realwave.sales.manager.events.utils

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.event.Event
import br.com.zup.realwave.event.Header
import br.com.zup.realwave.kserialize.json.jsonToObject
import br.com.zup.realwave.serialize.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object ParseEventUtils {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
}

fun <T> String.extractEvent(classs: Class<T>): Event<T> =
    try {
        val jsonNode = Json.toJsonNode(this)

        Event.builder<T>()
            .header(jsonNode.get("header").toString().jsonToObject(Header::class.java))
            .payload(jsonNode.get("payload").toString().jsonToObject(classs))
            .build()
    } catch (e: Exception) {
        ParseEventUtils.logger.error("Unexpected error to deserialize $this for ${classs.simpleName}", e)
        throw e
    }

fun loadContextVariables(header: Header) {
    RealwaveContextHolder
        .getContextHolder()
        .set(header.context)
}

inline fun <reified T> String.extractEvent(): Event<T> =
    this.extractEvent(T::class.java)
        .apply { loadContextVariables(header) }
