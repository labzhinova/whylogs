package com.whylabs.logging.core;

import com.whylabs.logging.core.data.ColumnSummary;
import java.time.Instant;
import java.util.Map;
import lombok.Value;

@Value
public class InterpretableDatasetProfile {

  String name;
  Instant timestamp;
  Map<String, ColumnSummary> columns;
}
