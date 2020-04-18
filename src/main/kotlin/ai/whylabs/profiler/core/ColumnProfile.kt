package ai.whylabs.profiler.core

import ai.whylabs.profiler.jvm.ColumnDataType
import ai.whylabs.profiler.jvm.InterpretableColumnStatistics
import ai.whylabs.profiler.jvm.summary.DoubleSummary
import ai.whylabs.profiler.jvm.summary.FrequentStringsSummary
import ai.whylabs.profiler.jvm.summary.HistogramSummary
import ai.whylabs.profiler.jvm.summary.LongSummary
import ai.whylabs.profiler.jvm.summary.QuantilesSummary
import ai.whylabs.profiler.jvm.summary.StandardDeviationSummary
import ai.whylabs.profiler.jvm.summary.UniqueCountSummary
import org.apache.datasketches.cpc.CpcSketch
import org.apache.datasketches.frequencies.ItemsSketch
import org.apache.datasketches.quantiles.DoublesSketch
import org.apache.datasketches.quantiles.UpdateDoublesSketch
import java.util.EnumMap

class ColumnProfile(val name: String) {
    private var totalCnt = 0L
    private val typeCounts = EnumMap<ColumnDataType, Long>(ColumnDataType::class.java)
    private val cpcSketch: CpcSketch = CpcSketch()
    private val stringSketch: ItemsSketch<String> = ItemsSketch(128)
    private val numbersSketch: UpdateDoublesSketch = DoublesSketch.builder()
        .setK(256).build()

    private val longSummary = LongSummary()
    private val doubleSummary = DoubleSummary()
    private val stddevSummary = StandardDeviationSummary()
    private var trueCnt = 0L
    private var nullCnt = 0L

    fun track(data: Any?) {
        longSummary
        val coercedData = coerceType(data)
        // match object based on class here
        when (coercedData) {
            is Number -> track(coercedData)
            is String -> track(coercedData)
            is Boolean -> track(coercedData)
            null -> trackNull()
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
                stddevSummary.update(value)
            }
            is Long -> {
                longSummary.update(value)
                cpcSketch.update(value)
                val asDoubleValue = value.toDouble()
                numbersSketch.update(asDoubleValue)
                stddevSummary.update(asDoubleValue)
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
                    IntegralPattern.containsMatchIn(data) -> data.toLong()
                    FractionalPattern.containsMatchIn(data) -> data.toDouble()
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

    private fun addTypeCount(dataType: ColumnDataType) {
        this.typeCounts.compute(dataType) { _: ColumnDataType, value: Long? -> value?.inc() ?: 1 }
    }

    fun toInterpretableStatistics(): InterpretableColumnStatistics {
        return InterpretableColumnStatistics.bd()
            .totalCount(totalCnt)
            .typeCounts(typeCounts)
            .nullCount(nullCnt)
            .trueCount(if (trueCnt == 0L) null else trueCnt)
            .longSummary(if (longSummary.count == 0L) null else longSummary)
            .doubleSummary(if (doubleSummary.count == 0L) null else doubleSummary)
            .uniqueCountSummary(UniqueCountSummary.fromCpcSketch(cpcSketch))
            .quantilesSummary(
                if (numbersSketch.n == 0L) null else QuantilesSummary.fromUpdateDoublesSketch(
                    numbersSketch
                )
            )
            .histogramSummary(
                if (numbersSketch.n >= 0L && numbersSketch.maxValue > numbersSketch.minValue) {
                    HistogramSummary.fromUpdateDoublesSketch(numbersSketch, stddevSummary.stddev())
                } else {
                    null
                }
            )
            .frequentStringsSummary(
                if (cpcSketch.estimate < 100) {
                    FrequentStringsSummary.fromStringSketch(stringSketch)
                } else {
                    FrequentStringsSummary.empty()
                }
            )
            .build()
    }

    companion object {
        val FractionalPattern = Regex("^[-+]?( )?\\d+([.]\\d+)$")
        val IntegralPattern = Regex("^[-+]?( )?\\d+$")
        val Boolean = Regex("^(?i)(true|false)$")
    }
}
