package ai.whylabs.profiler.core

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

internal class ByteArrayToBase64TypeAdapter : JsonSerializer<ByteArray>,
    JsonDeserializer<ByteArray> {
    override fun serialize(src: ByteArray?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(Base64.getEncoder().encodeToString(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ByteArray {
        return Base64.getDecoder().decode(json?.asString)
    }
}