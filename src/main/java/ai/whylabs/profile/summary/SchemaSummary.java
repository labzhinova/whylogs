package ai.whylabs.profile.summary;

import ai.whylabs.profile.statistics.schema.ColumnDataType;
import ai.whylabs.profile.statistics.schema.InferredType;
import ai.whylabs.profile.statistics.schema.SchemaTracker;
import java.util.Map;
import lombok.Value;

@Value
public class SchemaSummary {
  Map<ColumnDataType, Long> typeCounts;
  InferredType inferredType;
  InferredType determinedType;

  public static SchemaSummary fromTracker(SchemaTracker tracker, InferredType determinedType) {
    return new SchemaSummary(tracker.getTypeCounts(), tracker.getDeterminedType(), determinedType);
  }
}
