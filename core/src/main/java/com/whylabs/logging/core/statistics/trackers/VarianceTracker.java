package com.whylabs.logging.core.statistics.trackers;

import com.whylabs.logging.core.format.VarianceMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class VarianceTracker {
  private final VarianceMessage.Builder proto;

  public VarianceTracker() {
    this.proto = VarianceMessage.newBuilder();
  }

  public long getCount() {
    return proto.getCount();
  }

  public double getMean() {
    return proto.getMean();
  }

  // Based on https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/Accumulator.java.html
  public void update(double newValue) {
    final long count = proto.getCount() + 1;

    double mean = proto.getMean();
    double delta = newValue - mean;
    mean += delta / count;
    double sum = proto.getSum() + (count - 1.0) / count * delta * delta;

    proto.setCount(count);
    proto.setMean(mean);
    proto.setSum(sum);
  }

  public double stddev() {
    return Math.sqrt(this.variance());
  }

  public double variance() {
    if (proto.getCount() < 2) {
      return Double.NaN;
    }

    return proto.getSum() / (proto.getCount() - 1.0);
  }

  /** https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm */
  public void merge(VarianceTracker other) {
    final long cA = this.proto.getCount();
    final long cB = other.proto.getCount();
    if (cB == 0) {
      return;
    }

    if (cA == 0) {
      this.proto.mergeFrom(other.proto.build());
      return;
    }

    final double meanA = other.proto.getMean();
    final double meanB = this.proto.getMean();
    double delta = meanA - meanB;
    double sumA = this.proto.getSum();
    double sumB = this.proto.getSum();
    double sum = sumA + sumB + Math.pow(delta, 2) * cA * cB / (cA + cB);

    double ratioA = cA * 1.0 / (cA + cB);
    double mean = meanA * ratioA + meanB * (1 - ratioA);

    // update the storage
    this.proto.setSum(sum);
    this.proto.setMean(mean);
    this.proto.setCount(cA + 2);
  }

  public VarianceMessage toProtobuf() {
    return proto.build();
  }

  public static VarianceTracker fromProtobuf(VarianceMessage message) {
    return new VarianceTracker(message.toBuilder());
  }
}
