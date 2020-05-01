package com.whylabs.logging.core;

import com.whylabs.logging.core.data.ColumnSummary;
import com.whylabs.logging.core.data.DatasetSummary;
import com.whylabs.logging.core.format.DatasetProfileMessage;
import com.whylabs.logging.core.format.DatasetProfileMessage.Builder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.val;

public class DatasetProfile {

  String name;
  Instant timestamp;
  Map<String, ColumnProfile> columns;

  public DatasetProfile(String name, Instant timestamp) {
    this.name = name;
    this.timestamp = timestamp;
    this.columns = new HashMap<>();
  }

  public void track(String columnName, Object data) {
    trackSingleColumn(columnName, data);
  }

  private void trackSingleColumn(String columnName, Object data) {
    val columnProfile =
        columns.compute(
            columnName,
            (colName, existingProfile) ->
                (existingProfile == null) ? new ColumnProfile(columnName) : existingProfile);
    columnProfile.track(data);
  }

  public <T> void track(Map<String, T> columns) {
    columns.forEach(this::track);
  }

  public DatasetSummary toSummary() {
    val intpColumns =
        columns.values().stream()
            .map(Pair::fromColumn)
            .collect(Collectors.toMap(Pair::getName, Pair::getStatistics));

    return DatasetSummary.newBuilder()
        .setName(name)
        .setTimestamp(timestamp.toEpochMilli())
        .putAllColumns(intpColumns)
        .build();
  }

  public DatasetProfileMessage.Builder toProtobuf() {
    final Builder builder =
        DatasetProfileMessage.newBuilder().setName(name).setTimestamp(timestamp.toEpochMilli());
    columns.forEach((k, v) -> builder.putColumns(k, v.toProtobuf().build()));
    return builder;
  }

  @Value
  static class Pair {

    String name;
    ColumnSummary statistics;

    static Pair fromColumn(ColumnProfile column) {
      return new Pair(column.getColumnName(), column.toColumnSummary());
    }
  }
}
