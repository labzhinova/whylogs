package ai.whylabs.profile;

import ai.whylabs.profile.summary.NumberSummary;
import ai.whylabs.profile.summary.StringSummary;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InterpretableColumnStatistics {

  Long totalCount;
  Map<ColumnDataType, Long> typeCounts;
  NumberSummary numberSummary;
  StringSummary stringSummary;
  Long nullCount;
  Long trueCount;
}
