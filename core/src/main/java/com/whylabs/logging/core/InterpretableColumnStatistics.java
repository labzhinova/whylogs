package com.whylabs.logging.core;

import com.whylabs.logging.core.statistics.Counters;
import com.whylabs.logging.core.summary.NumberSummary;
import com.whylabs.logging.core.summary.SchemaSummary;
import com.whylabs.logging.core.summary.StringSummary;
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
