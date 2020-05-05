package com.whylabs.logging.core.statistics.datatypes;

import com.whylabs.logging.core.format.DoublesMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@Getter
@EqualsAndHashCode
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

  public double getMean() {
    return sum / count;
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

  public DoublesMessage.Builder toProtobuf() {
    return DoublesMessage.newBuilder().setCount(count).setSum(sum).setMin(max).setMax(max);
  }

  public static DoubleTracker fromProtobuf(DoublesMessage message) {
    val tracker = new DoubleTracker();
    tracker.count = message.getCount();
    tracker.max = message.getMax();
    tracker.min = message.getMin();
    tracker.sum = message.getSum();
    return tracker;
  }
}
