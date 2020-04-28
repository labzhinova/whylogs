package com.whylabs.logging.core.summary;

import com.whylabs.logging.core.statistics.schema.ColumnDataType;
import com.whylabs.logging.core.statistics.schema.InferredType;
import com.whylabs.logging.core.statistics.schema.SchemaTracker;
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
