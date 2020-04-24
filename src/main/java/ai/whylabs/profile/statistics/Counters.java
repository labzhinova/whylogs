package ai.whylabs.profile.statistics;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Counters {
  long count;

  // Nullable values
  Long trueCount;
  Long nullCount;

  public void incrementCount() {
    count++;
  }

  public void incrementTrue() {
    trueCount = (trueCount == null) ? 1 : trueCount + 1;
  }

  public void incrementNull() {
    nullCount = (nullCount == null) ? 1 : nullCount + 1;
  }
}
