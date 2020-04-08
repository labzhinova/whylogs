package ai.whylabs.profiler.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.util.Base64

internal class ByteArrayToBase64TypeAdapter : JsonSerializer<ByteArray>,
    JsonDeserializer<ByteArray> {
    override fun serialize(src: ByteArray?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(Base64.getEncoder().encodeToString(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ByteArray {
        return Base64.getDecoder().decode(json?.asString)
    }
}

internal class InstantToLongTypeAdapter : JsonSerializer<Instant>, JsonDeserializer<Instant> {
    override fun serialize(src: Instant?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toEpochMilli())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Instant {
        return Instant.ofEpochMilli(json?.asLong ?: 0L)
    }

}