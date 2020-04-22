package ai.whylabs.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.val;

public class DatasetProfile {

  public static Gson Gson =
      new GsonBuilder()
          .setPrettyPrinting()
          .serializeSpecialFloatingPointValues()
          .registerTypeAdapter(byte[].class, new Utils.ByteArrayToBase64TypeAdapter())
          .registerTypeAdapter(Instant.class, new Utils.InstantToLongTypeAdapter())
          .create();

  String name;
  Instant timestamp;
  Map<String, ColumnProfile> columns;
  long count;
  long lastCompactionCount;

  public DatasetProfile(String name, Instant timestamp) {
    this.name = name;
    this.timestamp = timestamp;
    this.columns = new HashMap<>();
    this.count = 0L;
    this.lastCompactionCount = 0L;
  }

  public void track(String columnName, Object data) {
    count++;
    trackSingleColumn(columnName, data);
    compact();
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
    count++;
    columns.forEach(this::track);
    compact();
  }

  private void compact() {
    if (count - lastCompactionCount > 1000) {
      lastCompactionCount = count;
      columns.values().forEach(ColumnProfile::compact);
    }
  }

  public InterpretableDatasetProfile toInterpretableObject() {
    val intpColumns =
        columns.values().stream()
            .map(Pair::fromColumn)
            .collect(Collectors.toMap(Pair::getName, Pair::getStatistics));

    return new InterpretableDatasetProfile(name, timestamp, intpColumns);
  }

  @Value
  static class Pair {

    String name;
    InterpretableColumnStatistics statistics;

    static Pair fromColumn(ColumnProfile column) {
      return new Pair(column.getColumnName(), column.toInterpretableStatistics());
    }
  }
}
