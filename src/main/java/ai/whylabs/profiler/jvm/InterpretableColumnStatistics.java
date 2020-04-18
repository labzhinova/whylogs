package ai.whylabs.profiler.jvm;

import ai.whylabs.profiler.jvm.summary.DoubleSummary;
import ai.whylabs.profiler.jvm.summary.FrequentStringsSummary;
import ai.whylabs.profiler.jvm.summary.HistogramSummary;
import ai.whylabs.profiler.jvm.summary.LongSummary;
import ai.whylabs.profiler.jvm.summary.QuantilesSummary;
import ai.whylabs.profiler.jvm.summary.UniqueCountSummary;
import lombok.Value;

import java.util.Map;

@Value
public final class InterpretableColumnStatistics {
    private final Long totalCount;
    private final Map<ColumnDataType, Long> typeCounts;
    private final Long nullCount;
    private final Long trueCount;
    private final LongSummary longSummary;
    private final DoubleSummary doubleSummary;
    private final UniqueCountSummary uniqueCountSummary;
    private final QuantilesSummary quantilesSummary;
    private final HistogramSummary histogramSummary;
    private final FrequentStringsSummary frequentStringsSummary;

    InterpretableColumnStatistics(Long totalCount, Map<ColumnDataType, Long> typeCounts, Long nullCount, Long trueCount, LongSummary longSummary, DoubleSummary doubleSummary, UniqueCountSummary uniqueCountSummary, QuantilesSummary quantilesSummary, HistogramSummary histogramSummary, FrequentStringsSummary frequentStringsSummary) {
        this.totalCount = totalCount;
        this.typeCounts = typeCounts;
        this.nullCount = nullCount;
        this.trueCount = trueCount;
        this.longSummary = longSummary;
        this.doubleSummary = doubleSummary;
        this.uniqueCountSummary = uniqueCountSummary;
        this.quantilesSummary = quantilesSummary;
        this.histogramSummary = histogramSummary;
        this.frequentStringsSummary = frequentStringsSummary;
    }

    public static InterpretableColumnStatistics.InterpretableColumnStatisticsBuilder bd() {
        return builder();
    }

    public static InterpretableColumnStatisticsBuilder builder() {
        return new InterpretableColumnStatisticsBuilder();
    }

    public static class InterpretableColumnStatisticsBuilder {
        private Long totalCount;
        private Map<ColumnDataType, Long> typeCounts;
        private Long nullCount;
        private Long trueCount;
        private LongSummary longSummary;
        private DoubleSummary doubleSummary;
        private UniqueCountSummary uniqueCountSummary;
        private QuantilesSummary quantilesSummary;
        private HistogramSummary histogramSummary;
        private FrequentStringsSummary frequentStringsSummary;

        InterpretableColumnStatisticsBuilder() {
        }

        public InterpretableColumnStatisticsBuilder totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public InterpretableColumnStatisticsBuilder typeCounts(Map<ColumnDataType, Long> typeCounts) {
            this.typeCounts = typeCounts;
            return this;
        }

        public InterpretableColumnStatisticsBuilder nullCount(Long nullCount) {
            this.nullCount = nullCount;
            return this;
        }

        public InterpretableColumnStatisticsBuilder trueCount(Long trueCount) {
            this.trueCount = trueCount;
            return this;
        }

        public InterpretableColumnStatisticsBuilder longSummary(LongSummary longSummary) {
            this.longSummary = longSummary;
            return this;
        }

        public InterpretableColumnStatisticsBuilder doubleSummary(DoubleSummary doubleSummary) {
            this.doubleSummary = doubleSummary;
            return this;
        }

        public InterpretableColumnStatisticsBuilder uniqueCountSummary(UniqueCountSummary uniqueCountSummary) {
            this.uniqueCountSummary = uniqueCountSummary;
            return this;
        }

        public InterpretableColumnStatisticsBuilder quantilesSummary(QuantilesSummary quantilesSummary) {
            this.quantilesSummary = quantilesSummary;
            return this;
        }

        public InterpretableColumnStatisticsBuilder histogramSummary(HistogramSummary histogramSummary) {
            this.histogramSummary = histogramSummary;
            return this;
        }

        public InterpretableColumnStatisticsBuilder frequentStringsSummary(FrequentStringsSummary frequentStringsSummary) {
            this.frequentStringsSummary = frequentStringsSummary;
            return this;
        }

        public InterpretableColumnStatistics build() {
            return new InterpretableColumnStatistics(totalCount, typeCounts, nullCount, trueCount, longSummary, doubleSummary, uniqueCountSummary, quantilesSummary, histogramSummary, frequentStringsSummary);
        }

        public String toString() {
            return "InterpretableColumnStatistics.InterpretableColumnStatisticsBuilder(totalCount=" + this.totalCount + ", typeCounts=" + this.typeCounts + ", nullCount=" + this.nullCount + ", trueCount=" + this.trueCount + ", longSummary=" + this.longSummary + ", doubleSummary=" + this.doubleSummary + ", uniqueCountSummary=" + this.uniqueCountSummary + ", quantilesSummary=" + this.quantilesSummary + ", histogramSummary=" + this.histogramSummary + ", frequentStringsSummary=" + this.frequentStringsSummary + ")";
        }
    }
}
