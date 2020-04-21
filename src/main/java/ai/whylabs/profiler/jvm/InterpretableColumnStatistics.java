package ai.whylabs.profiler.jvm;

import ai.whylabs.profiler.jvm.summary.DoubleSummary;
import ai.whylabs.profiler.jvm.summary.FrequentStringsSummary;
import ai.whylabs.profiler.jvm.summary.HistogramSummary;
import ai.whylabs.profiler.jvm.summary.LongSummary;
import ai.whylabs.profiler.jvm.summary.QuantilesSummary;
import ai.whylabs.profiler.jvm.summary.UniqueCountSummary;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value
public class InterpretableColumnStatistics {
    Long totalCount;
    Map<ColumnDataType, Long> typeCounts;
    Long nullCount;
    Long trueCount;
    LongSummary longSummary;
    DoubleSummary doubleSummary;
    UniqueCountSummary uniqueCountSummary;
    QuantilesSummary quantilesSummary;
    HistogramSummary histogramSummary;
    FrequentStringsSummary frequentStringsSummary;
}
