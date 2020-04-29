package com.whylabs.logging.core.statistics;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Counters {
  private final com.whylabs.logging.core.data.Counters.Builder proto;

  public Counters() {
    this.proto = com.whylabs.logging.core.data.Counters.newBuilder();
  }

  public void incrementCount() {
    proto.setCount(proto.getCount() + 1);
  }

  public void incrementTrue() {
    val builder = proto.getTrueCountBuilder();
    builder.setValue(builder.getValue() + 1);
  }

  public void incrementNull() {
    val builder = proto.getNullCountBuilder();
    builder.setValue(builder.getValue() + 1);
  }

  public com.whylabs.logging.core.data.Counters toProtobuf() {
    return proto.build();
  }

  public static Counters fromProtobuf(com.whylabs.logging.core.data.Counters message) {
    return new Counters(message.toBuilder());
  }

  public long getCount() {
    return proto.getCount();
  }

  public Long getTrueCount() {
    if (proto.hasTrueCount()) {
      return proto.getTrueCount().getValue();
    }
    return null;
  }

  public Long getNullCount() {
    if (proto.hasNullCount()) {
      return proto.getNullCount().getValue();
    }
    return null;
  }
}
