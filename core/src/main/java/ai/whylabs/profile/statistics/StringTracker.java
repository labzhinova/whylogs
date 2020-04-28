package ai.whylabs.profile.statistics;

import lombok.Getter;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.theta.UpdateSketch;

@Getter
public final class StringTracker {

  private long count;

  // sketches
  private final ItemsSketch<String> stringsSketch;
  private final UpdateSketch thetaSketch;

  public StringTracker() {
    this.count = 0L;
    this.stringsSketch = new ItemsSketch<>(32); // TODO: make this value configurable
    this.thetaSketch = UpdateSketch.builder().build();
  }

  public void update(String value) {
    if (value == null) {
      return;
    }

    count++;
    thetaSketch.update(value);
    stringsSketch.update(value);
  }
}
