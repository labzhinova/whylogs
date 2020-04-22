package ai.whylabs.profile;

import ai.whylabs.profile.statistics.ColumnDataType;
import ai.whylabs.profile.statistics.Counters;
import ai.whylabs.profile.summary.NumberSummary;
import ai.whylabs.profile.summary.StringSummary;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InterpretableColumnStatistics {

  Counters counters;
  Map<ColumnDataType, Long> typeCounts;
  NumberSummary numberSummary;
  StringSummary stringSummary;
}
