package br.com.zup.realwave.sales.manager.domain

import com.fasterxml.jackson.databind.JsonNode

data class Callback(val url: String, val headers: JsonNode?)
