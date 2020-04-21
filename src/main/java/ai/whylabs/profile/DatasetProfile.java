package ai.whylabs.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasetProfile {
    String name;
    Instant timestamp;
    Map<String, ColumnProfile> columns;

    public DatasetProfile(String name, Instant timestamp) {
        this(name, timestamp, new ConcurrentHashMap<>());
    }

    public void track(String columnName, Object data) {
        val columnProfile = columns.compute(columnName,
                (colName, columnProfile1) -> (columnProfile1 == null) ? new ColumnProfile(columnName) : columnProfile1);
        columnProfile.track(data);
    }

    public <T> void track(Map<String, T> columns) {
        columns.forEach(this::track);
    }

    public InterpretableDatasetProfile toInterpretableObject() {
        val intpColumns = columns.entrySet().stream().map(entry -> new Pair(entry.getKey(), entry.getValue().toInterpretableStatistics()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        return new InterpretableDatasetProfile(name, timestamp, intpColumns);
    }

    @Value
    static class Pair {
        String key;
        InterpretableColumnStatistics value;
    }


    public static Gson Gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(
                    byte[].class, new Utils.ByteArrayToBase64TypeAdapter()
            )
            .registerTypeAdapter(
                    Instant.class, new Utils.InstantToLongTypeAdapter()
            )
            .create();

}
