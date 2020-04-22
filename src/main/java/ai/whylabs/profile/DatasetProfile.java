package ai.whylabs.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

  public DatasetProfile(String name, Instant timestamp) {
    this(name, timestamp, new ConcurrentHashMap<>());
  }

  public void track(String columnName, Object data) {
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
