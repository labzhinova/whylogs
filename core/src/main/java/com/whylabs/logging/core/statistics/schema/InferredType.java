package com.whylabs.logging.core.statistics.schema;

import lombok.Value;

@Value
public class InferredType {
  ColumnDataType type;
  double ratio;
  long count;
}
