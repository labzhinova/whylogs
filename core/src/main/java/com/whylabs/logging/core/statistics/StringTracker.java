package com.whylabs.logging.core.statistics;

import com.google.protobuf.ByteString;
import com.whylabs.logging.core.format.StringsMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.apache.datasketches.ArrayOfStringsSerDe;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.theta.UpdateSketch;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringTracker {
  public static final ArrayOfStringsSerDe ARRAY_OF_STRINGS_SER_DE = new ArrayOfStringsSerDe();

  private long count;

  // sketches
  private final ItemsSketch<String> items;
  private final UpdateSketch thetaSketch;

  public StringTracker() {
    this.count = 0L;
    this.items = new ItemsSketch<>(32); // TODO: make this value configurable
    this.thetaSketch = UpdateSketch.builder().build();
  }

  public void update(String value) {
    if (value == null) {
      return;
    }

    count++;
    thetaSketch.update(value);
    items.update(value);
  }

  public StringsMessage.Builder toProtobuf() {
    return StringsMessage.newBuilder()
        .setCount(count)
        .setItems(ByteString.copyFrom(items.toByteArray(ARRAY_OF_STRINGS_SER_DE)))
        .setTheta(ByteString.copyFrom(thetaSketch.toByteArray()));
  }

  public static StringTracker fromProtobuf(StringsMessage message) {
    val iMem = Memory.wrap(message.getItems().toByteArray());
    val items = ItemsSketch.getInstance(iMem, ARRAY_OF_STRINGS_SER_DE);
    val tMem = WritableMemory.wrap(message.getTheta().toByteArray());
    val theta = UpdateSketch.wrap(tMem);

    return StringTracker.builder()
        .count(message.getCount())
        .items(items)
        .thetaSketch(theta)
        .build();
  }
}
