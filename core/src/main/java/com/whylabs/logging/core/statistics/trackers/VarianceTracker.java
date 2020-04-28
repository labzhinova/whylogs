package com.whylabs.logging.core.statistics.trackers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class VarianceTracker {

  private double sum;
  private double m2;
  private double mean;
  private long n;

  public VarianceTracker() {
    this(0.0, 0.0, 0.0, 0L);
  }

  public void update(double value) {
    n++;
    sum += value;
    val delta = value - mean;
    mean += delta / n;
    m2 += delta * (value - mean);
  }

  public Double value() {
    if (n < 2) {
      return null;
    }
    return Math.sqrt(m2 / (n - 1.0));
  }
}
