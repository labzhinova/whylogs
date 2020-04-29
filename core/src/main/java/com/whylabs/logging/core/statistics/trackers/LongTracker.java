package com.whylabs.logging.core.statistics.trackers;

import com.whylabs.logging.core.format.LongsMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class LongTracker {
  private final LongsMessage.Builder proto;

  public LongTracker() {
    this.proto = LongsMessage.newBuilder();
    reset();
  }

  public long getCount() {
    return this.proto.getCount();
  }

  public long getMin() {
    return this.proto.getMin();
  }

  public long getMax() {
    return this.proto.getMax();
  }

  public long getSum() {
    return this.proto.getSum();
  }

  public Double getMean() {
    if (proto.getCount() == 0) {
      return null;
    }

    return proto.getSum() * 1.0 / proto.getCount();
  }

  public void update(long value) {
    if (value > proto.getMax()) {
      proto.setMax(value);
    }
    if (value < proto.getMin()) {
      proto.setMin(value);
    }

    proto.setCount(proto.getCount() + 1);
    proto.setSum(proto.getSum() + value);
  }

  public void reset() {
    proto.clear();
    proto.setMin(Long.MAX_VALUE).setMax(Long.MIN_VALUE);
  }

  public LongsMessage toProtobuf() {
    return proto.build();
  }

  public static LongTracker fromProtobuf(LongsMessage message) {
    return new LongTracker(message.toBuilder());
  }
}
