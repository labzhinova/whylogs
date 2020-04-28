package ai.whylabs.profile;

import ai.whylabs.profile.statistics.Counters;
import ai.whylabs.profile.summary.NumberSummary;
import ai.whylabs.profile.summary.SchemaSummary;
import ai.whylabs.profile.summary.StringSummary;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InterpretableColumnStatistics {

  Counters counters;
  SchemaSummary schema;
  NumberSummary numberSummary;
  StringSummary stringSummary;
}
