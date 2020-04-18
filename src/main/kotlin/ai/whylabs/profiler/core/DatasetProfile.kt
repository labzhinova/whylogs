package ai.whylabs.profiler.core

import ai.whylabs.profiler.jvm.InterpretableColumnStatistics
import ai.whylabs.profiler.jvm.Utils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

// a data class that can be rendered by Gson
data class InterpretableDatasetProfile(
    val name: String?,
    val timestamp: Instant,
    val columns: Map<String, InterpretableColumnStatistics>
)

class DatasetProfile(val name: String?, val timestamp: Instant) {
    private val columns: MutableMap<String, ColumnProfile> = ConcurrentHashMap()

    fun track(colName: String, data: Any?) {
        columns.compute(colName) { _, col ->
            (col?.apply { track(data) }) ?: ColumnProfile(colName).apply { track(data) }
        }
    }

    fun track(columns: Map<String, Any?>) {
        columns.forEach { (column, value) -> track(column, value) }
    }

    fun toInterpretableObject(): InterpretableDatasetProfile {
        val intpColStats = columns.mapValues { (_, column) -> column.toInterpretableStatistics() }

        return InterpretableDatasetProfile(name, timestamp, intpColStats)
    }

    fun toJsonString(): String {
        return Gson.toJson(this.toInterpretableObject())
    }

    companion object {
        val Gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(
                ByteArray::class.java, Utils.ByteArrayToBase64TypeAdapter()
            )
            .registerTypeAdapter(
                Instant::class.java, Utils.InstantToLongTypeAdapter()
            )
            .create()
    }
}