package com.whylabs.logging.core.statistics.trackers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DoubleTracker {

  private double min;
  private double max;
  private double sum;
  private long count;

  public DoubleTracker() {
    this.min = Double.MAX_VALUE;
    this.max = -Double.MAX_VALUE;
    this.sum = 0;
    this.count = 0;
  }

  public void addLongs(LongTracker longs) {
    if (longs != null && longs.getCount() != 0) {
      this.min = longs.getMin();
      this.max = longs.getMax();
      this.sum = longs.getSum();
      this.count = longs.getCount();
    }
  }

  public Double getMean() {
    if (count == 0) {
      return null;
    } else {
      return sum / count;
    }
  }

  public void update(double value) {
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
