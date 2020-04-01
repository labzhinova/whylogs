package ai.whylabs.profiler.core

import com.google.gson.GsonBuilder
import java.util.concurrent.ConcurrentHashMap

class DatasetProfile(val name: String?) {
    val columns: MutableMap<String, ColumnProfile> = ConcurrentHashMap()

    fun track(column: String, data: Any?) {
        columns.compute(column) { _, c -> c?.apply { track(data) } ?: ColumnProfile(column).apply { track(data) } }
    }

    fun track(columns: Map<String, Any?>) {
        columns.forEach { (column, value) -> track(column, value) }
    }

    fun toJsonString(): String {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(
                ByteArray::class.java, ByteArrayToBase64TypeAdapter()
            )
            .create()

        return gsonPretty.toJson(this)
    }
}