package ai.whylabs.profiler.core

import com.google.gson.GsonBuilder
import java.util.concurrent.ConcurrentHashMap

data class InterpretableDatasetProfile(val name: String?, val columns: Map<String, InterpretableColumnStatistics>)

class DatasetProfile(val name: String?) {
    private val columns: MutableMap<String, ColumnProfile> = ConcurrentHashMap()

    fun track(colName: String, data: Any?) {
        columns.compute(colName) { _, col ->
            (col?.apply { track(data) }) ?: ColumnProfile(colName).apply { track(data) }
        }
    }

    fun track(columns: Map<String, Any?>) {
        columns.forEach { (column, value) -> track(column, value) }
    }

    fun toJsonString(): String {
        val intpColStats = columns.mapValues { (_, column) -> column.toInterpretableStatistics() }

        return Gson.toJson(InterpretableDatasetProfile(name, intpColStats))
    }

    companion object {
        private val Gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(
                ByteArray::class.java, ByteArrayToBase64TypeAdapter()
            )
            .create()

    }
}