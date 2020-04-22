package ai.whylabs.profile;

import ai.whylabs.profile.summary.FrequentStringsSummary;
import ai.whylabs.profile.summary.NumberSummary;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InterpretableColumnStatistics {

  Long totalCount;
  Map<ColumnDataType, Long> typeCounts;
  NumberSummary numberSummary;
  Long nullCount;
  Long trueCount;
  FrequentStringsSummary frequentStringsSummary;
}
