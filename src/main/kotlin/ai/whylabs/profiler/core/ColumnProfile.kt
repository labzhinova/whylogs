package ai.whylabs.profiler.core

import com.google.gson.GsonBuilder
import org.apache.datasketches.cpc.CpcSketch
import org.apache.datasketches.frequencies.ErrorType
import org.apache.datasketches.frequencies.ItemsSketch
import org.apache.datasketches.quantiles.DoublesSketch
import org.apache.datasketches.quantiles.UpdateDoublesSketch


enum class ColumnDataType {
    NULL,
    FRACTIONAL,
    INTEGRAL,
    BOOLEAN,
    STRING,
    UNKNOWN,
}

class LongSummary {
    private var max = Long.MIN_VALUE
    private var min = Long.MAX_VALUE
    private var sum = 0L
    private var count = 0L

    fun update(value: Long) {
        if (value > max) max = value
        if (min < value) min = value
        sum += value
        count++
    }
}

class DoubleSummary {
    private var max = Double.MIN_VALUE
    private var min = Double.MAX_VALUE
    private var sum = 0.0
    private var count = 0.0

    fun update(value: Double) {
        if (value > max) max = value
        if (min < value) min = value
        sum += value
        count++
    }

}

internal data class SerializableColumnProfile(
    val totalCount: Long,
    val typeCounts: LongArray,
    val longSummary: LongSummary?,
    val doubleSummary: DoubleSummary?,
    val trueCount: Long?,
    val nullCount: Long?,
    val cpcSketchBytes: ByteArray?,
    val stringSketchBytes: ByteArray?
)


class ColumnProfile(val name: String) {
    private var totalCnt = 0L
    private val typeCounts: LongArray = LongArray(ColumnDataType.values().size)
    private val cpcSketch: CpcSketch = CpcSketch()
    private val stringSketch: ItemsSketch<String> = ItemsSketch(128)
    private val numbersSketch: UpdateDoublesSketch = DoublesSketch.builder()
        .setK(256).build()

    private val longSummary = LongSummary()
    private val doubleSummary = DoubleSummary()
    private var trueCnt = 0L
    private var nullCnt = 0L

    fun track(data: Any?) {
        longSummary
        val coercedData = coerceType(data)
        when (coercedData) {
            null -> trackNull()
            else -> track(coercedData)
        }
        addTypeCount(detectType(coercedData))
        totalCnt++
    }

    private fun track(value: Number) {
        when (value) {
            is Double -> {
                doubleSummary.update(value)
                cpcSketch.update(value)
                numbersSketch.update(value)
            }
            is Long -> {
                longSummary.update(value)
                cpcSketch.update(value)
                numbersSketch.update(value.toDouble())
            }
        }
    }

    private fun track(text: String) {
        cpcSketch.update(text)
        stringSketch.update(text)
    }

    private fun track(flag: Boolean) {
        if (flag) {
            trueCnt++
        }
    }

    private fun trackNull() {
        nullCnt++
    }

    private fun coerceType(data: Any?): Any? {
        return when (data) {
            is String -> {
                when {
                    FractionalPattern.containsMatchIn(data) -> data.toLong()
                    IntegralPattern.containsMatchIn(data) -> data.toDouble()
                    Boolean.containsMatchIn(data) -> data.toBoolean()
                    else -> data
                }
            }
            is Int, is Long, is Short -> {
                (data as Number).toLong()
            }
            is Double, is Float -> (data as Number).toDouble()
            is Boolean -> data
            else -> data
        }

    }

    private fun detectType(data: Any?): ColumnDataType {
        return when (data) {
            is String -> ColumnDataType.STRING
            is Long -> ColumnDataType.INTEGRAL
            is Double -> ColumnDataType.FRACTIONAL
            is Boolean -> ColumnDataType.BOOLEAN
            null -> ColumnDataType.NULL
            else -> ColumnDataType.UNKNOWN
        }
    }

    private fun addTypeCount(columnDataType: ColumnDataType) {
        this.typeCounts[columnDataType.ordinal]++
    }

    fun toJsonString(): String {
        val gsonPretty = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(
                ByteArray::class.java, ByteArrayToBase64TypeAdapter()
            )
            .create()

        val statistics = InterpretableColumnStatistics(
            totalCount = totalCnt,
            typeCounts = emptyMap(),
            longSummary = longSummary,
            doubleSummary = doubleSummary,
            uniqueCountSummary = UniqueCountSummary.fromCpcSketch(cpcSketch),
            quantilesSummary = QuantilesSummary.fromUpdateDoublesSketch(numbersSketch),
            uniqueStringsSummary = UniqueStringsSummary.fromStringSketch(stringSketch)
        )

        return gsonPretty.toJson(statistics)
    }
}

data class UniqueCountSummary(val estimate: Double, val upperbound: Double, val lowerBound: Double) {
    companion object {
        fun fromCpcSketch(cpcSketch: CpcSketch): UniqueCountSummary {
            return UniqueCountSummary(cpcSketch.estimate, cpcSketch.getUpperBound(2), cpcSketch.getLowerBound(2))
        }
    }
}

data class QuantilesSummary(val quantiles: List<Double>) {
    companion object {
        fun fromUpdateDoublesSketch(sketch: UpdateDoublesSketch): QuantilesSummary {
            return QuantilesSummary(sketch.getQuantiles(100)?.toList().orEmpty())

        }
    }
}

data class UniqueStringsSummary(val items: List<String>) {
    companion object {
        fun fromStringSketch(sketch: ItemsSketch<String>): UniqueStringsSummary {
            val items = sketch.getFrequentItems(ErrorType.NO_FALSE_NEGATIVES).map { row -> row.item }.toList()
            return UniqueStringsSummary(items)
        }
    }
}

data class InterpretableColumnStatistics(
    val totalCount: Long,
    val typeCounts: Map<ColumnDataType, Long>,
    val longSummary: LongSummary?,
    val doubleSummary: DoubleSummary?,
    val uniqueCountSummary: UniqueCountSummary,
    val quantilesSummary: QuantilesSummary,
    val uniqueStringsSummary: UniqueStringsSummary
)