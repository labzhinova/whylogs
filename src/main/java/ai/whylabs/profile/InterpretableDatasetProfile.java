package ai.whylabs.profile;

import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
public class InterpretableDatasetProfile {
    String name;
    Instant timestamp;
    Map<String, InterpretableColumnStatistics> columns;
}
