package com.whylabs.logging.core.statistics.datatypes;

import com.whylabs.logging.core.format.DoublesMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DoubleTracker {
  private final DoublesMessage.Builder proto;

  public DoubleTracker() {
    this.proto = DoublesMessage.newBuilder().setMin(Double.MAX_VALUE).setMax(-Double.MAX_VALUE);
  }

  public void addLongs(LongTracker longs) {
    if (longs != null && longs.getCount() != 0) {
      this.proto.setMin(longs.getMin());
      this.proto.setMax(longs.getMax());
      this.proto.setSum(longs.getSum());
      this.proto.setCount(longs.getCount());
    }
  }

  public Double getMean() {
    if (proto.getCount() == 0) {
      return null;
    } else {
      return proto.getSum() / proto.getCount();
    }
  }

  public void update(double value) {
    if (value > proto.getMax()) {
      proto.setMax(value);
    }
    if (value < proto.getMin()) {
      proto.setMin(value);
    }
    proto.setCount(proto.getCount() + 1);
    proto.setSum(proto.getSum() + value);
  }

  public DoublesMessage toProtobuf() {
    return proto.build();
  }

  public static DoubleTracker fromProtobuf(DoublesMessage message) {
    return new DoubleTracker(message.toBuilder());
  }

  public long getCount() {
    return proto.getCount();
  }

  public double getMin() {
    return proto.getMin();
  }

  public double getMax() {
    return proto.getMax();
  }
}
