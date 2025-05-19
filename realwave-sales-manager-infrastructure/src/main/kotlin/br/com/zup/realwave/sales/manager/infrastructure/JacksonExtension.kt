package br.com.zup.realwave.sales.manager.infrastructure

import br.com.zup.realwave.serialize.json.Json
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule

object JacksonExtension {

    val jacksonObjectMapper: ObjectMapper by lazy {
        Json.builder().registerModule(KotlinModule())
    }

}

fun <T> String.jsonToListObject(t: Class<T>): List<T> {
    val valueType = TypeFactory.defaultInstance().constructCollectionType(List::class.java, t)
    return JacksonExtension.jacksonObjectMapper.readValue<List<T>>(this, valueType)
}

fun <T> String.jsonToObject(t: TypeReference<T>): T =
    JacksonExtension.jacksonObjectMapper.readValue(this, t)

fun <T> String.jsonToObject(t: Class<T>): T =
    JacksonExtension.jacksonObjectMapper.readValue(this, t)

fun <T> T.objectToJson(): String =
    JacksonExtension.jacksonObjectMapper.writeValueAsString(this)

fun <T> List<T>.convertValue(): JsonNode =
    JacksonExtension.jacksonObjectMapper.convertValue(this, JsonNode::class.java)

fun <T> HashMap<String, T>.toJsonNode(): JsonNode =
    JacksonExtension.jacksonObjectMapper.valueToTree<JsonNode>(this)

fun <T> Map<String, T>.toJsonNode(): JsonNode =
    JacksonExtension.jacksonObjectMapper.valueToTree<JsonNode>(this)

fun <T : JsonNode> Map<String, Any>.valueToTree(): T =
    JacksonExtension.jacksonObjectMapper.valueToTree(this)

fun <T : JsonNode> List<Any>.valueToTree(): T =
    JacksonExtension.jacksonObjectMapper.valueToTree(this)

fun <T : Map<String, Any>> JsonNode.toMap(): T =
    JacksonExtension.jacksonObjectMapper.convertValue(this, object : TypeReference<Map<String, Any>>() {})

fun <T> String.jsonToObjectOrNull(t: Class<T>): T? =
    try {
        JacksonExtension.jacksonObjectMapper.readValue(this, t)
    } catch (e: JsonProcessingException) {
        null
    }

