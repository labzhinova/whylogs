package com.whylabs.logging.core.statistics;

import com.google.protobuf.ByteString;
import com.whylabs.logging.core.format.NumbersMessage;
import com.whylabs.logging.core.statistics.datatypes.DoubleTracker;
import com.whylabs.logging.core.statistics.datatypes.LongTracker;
import com.whylabs.logging.core.statistics.datatypes.VarianceTracker;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;
import org.apache.datasketches.theta.UpdateSketch;

@Getter
@Builder(setterPrefix = "set")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberTracker {
  // our own trackers
  VarianceTracker variance;
  DoubleTracker doubles;
  LongTracker longs;

  // sketches
  UpdateDoublesSketch histogram; // histogram
  UpdateSketch thetaSketch;

  public NumberTracker() {
    this.variance = new VarianceTracker();
    this.doubles = new DoubleTracker();
    this.longs = new LongTracker();

    this.thetaSketch = UpdateSketch.builder().build();
    this.histogram = DoublesSketch.builder().setK(256).build();
  }

  public void track(Number number) {
    double dValue = number.doubleValue();
    variance.update(dValue);
    thetaSketch.update(dValue);
    histogram.update(dValue);

    if (doubles.getCount() > 0) {
      doubles.update(dValue);
    } else if (number instanceof Long || number instanceof Integer) {
      longs.update(number.longValue());
    } else {
      doubles.addLongs(longs);
      longs.reset();
      doubles.update(dValue);
    }
  }

  public NumbersMessage.Builder toProtobuf() {
    val builder =
        NumbersMessage.newBuilder()
            .setVariance(variance.toProtobuf())
            .setTheta(ByteString.copyFrom(thetaSketch.toByteArray()))
            .setHistogram(ByteString.copyFrom(histogram.toByteArray()));

    if (this.doubles.getCount() > 0) {
      builder.setDoubles(this.doubles.toProtobuf());
    } else if (this.longs.getCount() > 0) {
      builder.setLongs(this.longs.toProtobuf());
    }

    return builder;
  }

  public static NumberTracker fromProtobuf(NumbersMessage message) {
    val tMem = WritableMemory.wrap(message.getTheta().toByteArray());
    val hMem = Memory.wrap(message.getHistogram().toByteArray());
    val builder =
        NumberTracker.builder()
            .setThetaSketch(UpdateSketch.wrap(tMem))
            .setHistogram(UpdateDoublesSketch.heapify(hMem))
            .setVariance(VarianceTracker.fromProtobuf(message.getVariance()));

    Optional.ofNullable(message.getDoubles())
        .map(DoubleTracker::fromProtobuf)
        .ifPresent(builder::setDoubles);
    Optional.ofNullable(message.getLongs())
        .map(LongTracker::fromProtobuf)
        .ifPresent(builder::setLongs);

    return builder.build();
  }
}
