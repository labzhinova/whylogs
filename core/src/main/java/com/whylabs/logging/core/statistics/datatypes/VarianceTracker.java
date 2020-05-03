package com.whylabs.logging.core.statistics.datatypes;

import com.whylabs.logging.core.format.VarianceMessage;
import lombok.Getter;

@Getter
public class VarianceTracker {
  long count;
  double sum; // sample variance * (n-1)
  double mean;

  public VarianceTracker() {
    this.count = 0L;
    this.sum = 0L;
    this.mean = 0L;
  }

  // Based on
  // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
  public void update(double newValue) {
    count++;

    double delta = newValue - mean;
    mean += delta / count;
    double delta2 = newValue - mean;
    sum += delta * delta2;
  }

  /** @return sample standard deviation */
  public double stddev() {
    return Math.sqrt(this.variance());
  }

  /** @return the sample variance */
  public double variance() {
    if (count < 2) {
      return Double.NaN;
    }

    return sum / (count - 1.0);
  }

  /** https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm */
  public void merge(VarianceTracker other) {
    if (other.count == 0L) {
      return;
    }

    if (this.count == 0L) {
      this.count = other.count;
      this.sum = other.sum;
      this.mean = other.mean;
      return;
    }

    final double delta = this.mean - other.mean;
    final long totalCount = this.count + other.count;
    this.sum += other.sum + Math.pow(delta, 2) * this.count * other.count / (double) totalCount;

    final double thisRatio = this.count / (double) totalCount;
    final double otherRatio = 1.0 - thisRatio;
    this.mean = this.mean * thisRatio + other.mean * otherRatio;
    this.count += other.count;
  }

  public VarianceMessage.Builder toProtobuf() {
    return VarianceMessage.newBuilder().setCount(count).setMean(mean).setSum(sum);
  }

  public static VarianceTracker fromProtobuf(VarianceMessage message) {
    final VarianceTracker tracker = new VarianceTracker();
    tracker.count = message.getCount();
    tracker.mean = message.getMean();
    tracker.sum = message.getSum();
    return tracker;
  }
}
