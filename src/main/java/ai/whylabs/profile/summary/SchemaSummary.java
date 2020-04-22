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

  public static SchemaSummary fromTracker(SchemaTracker tracker) {
    return new SchemaSummary(tracker.getTypeCounts(), tracker.getInferredType());
  }
}
