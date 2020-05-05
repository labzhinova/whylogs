package com.whylabs.logging.core.statistics;

import com.google.protobuf.Int64Value;
import com.whylabs.logging.core.data.Counters;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

@EqualsAndHashCode
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountersTracker {
  long count;
  long trueCount;
  long nullCount;

  public void incrementCount() {
    count++;
  }

  public void incrementTrue() {
    trueCount++;
  }

  public void incrementNull() {
    nullCount++;
  }

  public Counters.Builder toProtobuf() {
    val countersBuilder = Counters.newBuilder().setCount(count);

    if (trueCount > 0) {
      countersBuilder.setTrueCount(Int64Value.of(trueCount));
    }

    if (nullCount > 0) {
      countersBuilder.setTrueCount(Int64Value.of(nullCount));
    }

    return countersBuilder;
  }

  public static CountersTracker fromProtobuf(Counters message) {
    val tracker = new CountersTracker();
    tracker.count = message.getCount();
    tracker.trueCount =
        Optional.ofNullable(message.getTrueCount()).map(Int64Value::getValue).orElse(0L);
    tracker.nullCount =
        Optional.ofNullable(message.getNullCount()).map(Int64Value::getValue).orElse(0L);

    return tracker;
  }
}
