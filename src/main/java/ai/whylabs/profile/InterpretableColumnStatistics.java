package ai.whylabs.profile;

import ai.whylabs.profile.stastistics.DoubleSummary;
import ai.whylabs.profile.stastistics.LongSummary;
import ai.whylabs.profile.summary.FrequentStringsSummary;
import ai.whylabs.profile.summary.HistogramSummary;
import ai.whylabs.profile.summary.QuantilesSummary;
import ai.whylabs.profile.summary.UniqueCountSummary;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

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
