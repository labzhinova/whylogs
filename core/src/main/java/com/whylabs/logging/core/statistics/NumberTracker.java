package com.whylabs.logging.core.statistics;

import com.whylabs.logging.core.statistics.trackers.DoubleTracker;
import com.whylabs.logging.core.statistics.trackers.LongTracker;
import com.whylabs.logging.core.statistics.trackers.VarianceTracker;
import lombok.Getter;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;
import org.apache.datasketches.theta.UpdateSketch;

@Getter
public class NumberTracker {
  // our own trackers
  private VarianceTracker stddev;
  private DoubleTracker doubles;
  private LongTracker longs;

  // sketches
  private UpdateDoublesSketch numbersSketch; // histogram
  private UpdateSketch thetaSketch;

  public NumberTracker() {
    this.stddev = new VarianceTracker();
    this.doubles = new DoubleTracker();
    this.longs = new LongTracker();

    this.thetaSketch = UpdateSketch.builder().build();
    this.numbersSketch = DoublesSketch.builder().setK(256).build();
  }

  public void track(Number number) {
    double dValue = number.doubleValue();
    stddev.update(dValue);
    thetaSketch.update(dValue);
    numbersSketch.update(dValue);

    if (doubles.getCount() > 0) {
      doubles.update(dValue);
    } else if (number instanceof Long || number instanceof Integer) {
      longs.update(number.longValue());
    } else {
      doubles.addLongs(longs);
      longs.reset();
      doubles.update(dValue);
    }
  }
}
