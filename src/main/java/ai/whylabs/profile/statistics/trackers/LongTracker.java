package ai.whylabs.profile.statistics.trackers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class LongTracker {

  private long min;
  private long max;
  private long sum;
  private long count;

  public LongTracker() {
    reset();
  }

  public Double getMean() {
    if (count == 0) {
      return null;
    } else {
      return sum * 1.0 / count;
    }
  }

  public void update(long value) {
    if (value > max) {
      max = value;
    }
    if (value < min) {
      min = value;
    }
    count++;
    sum += value;
  }

  public void reset() {
    min = Long.MAX_VALUE;
    max = Long.MIN_VALUE;
    sum = 0;
    count = 0;
  }
}
