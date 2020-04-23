package ai.whylabs.profile.statistics.schema;

import lombok.Value;

@Value
public class InferredType {
  ColumnDataType type;
  double ratio;
  long count;
}
