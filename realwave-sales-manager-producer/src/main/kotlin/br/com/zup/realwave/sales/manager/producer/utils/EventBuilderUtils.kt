package br.com.zup.realwave.sales.manager.producer.utils

import br.com.zup.realwave.common.context.RealwaveContextHolder
import br.com.zup.realwave.event.Event
import br.com.zup.realwave.event.Header
import java.time.LocalDateTime

fun <T> eventBuilder(eventId: String, eventType: String, domain: String, timestamp: LocalDateTime, payload: T) =
    Event.builder<T>()
        .header(eventHeaderBuilder(eventId, eventType, domain, timestamp))
        .payload(payload)
        .build()
        .toJson()

fun eventHeaderBuilder(eventId: String, eventType: String, domain: String, timestamp: LocalDateTime) =
    Header.builder()
        .eventId(eventId)
        .eventType(eventType)
        .timestamp(timestamp)
        .domain(domain)
        .context(RealwaveContextHolder.getContext())
        .build()
