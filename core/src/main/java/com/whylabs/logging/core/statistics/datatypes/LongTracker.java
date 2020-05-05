package com.whylabs.logging.core.statistics.datatypes;

import com.whylabs.logging.core.format.LongsMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@Getter
@EqualsAndHashCode
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
      return sum / (double) count;
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

  public LongsMessage.Builder toProtobuf() {
    return LongsMessage.newBuilder().setCount(count).setSum(sum).setMin(min).setMax(max);
  }

  public static LongTracker fromProtobuf(LongsMessage message) {
    val tracker = new LongTracker();
    tracker.count = message.getCount();
    tracker.max = message.getMax();
    tracker.min = message.getMin();
    tracker.sum = message.getSum();

    return tracker;
  }
}
