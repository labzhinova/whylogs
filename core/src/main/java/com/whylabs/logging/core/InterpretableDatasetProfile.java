package com.whylabs.logging.core;

import java.time.Instant;
import java.util.Map;
import lombok.Value;

@Value
public class InterpretableDatasetProfile {

  String name;
  Instant timestamp;
  Map<String, InterpretableColumnStatistics> columns;
}
