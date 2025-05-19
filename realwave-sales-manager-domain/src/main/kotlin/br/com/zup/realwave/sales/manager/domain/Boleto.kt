package br.com.zup.realwave.sales.manager.domain

import com.fasterxml.jackson.databind.JsonNode

/**
 * Created by Vitor B. Gonçalves on 27/06/18
 */
data class Boleto(val methodId: String,
                  val payload: JsonNode)
