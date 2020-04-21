package ai.whylabs.profile.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LongSummary {

  @Getter
  public long count;
  private long max;
  private long min;
  private long sum;

  public LongSummary() {
    this(Long.MIN_VALUE, Long.MAX_VALUE, 0L, 0L);
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
}
