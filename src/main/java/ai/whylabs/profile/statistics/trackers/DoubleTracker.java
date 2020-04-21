package ai.whylabs.profile.statistics.trackers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DoubleTracker implements KryoSerializable {

  private double min;
  private double max;
  private double sum;
  private long count;

  public DoubleTracker() {
    this(Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0L);
  }

  public void addLongs(LongTracker longs) {
    if (longs != null && longs.getCount() != 0) {
      this.min = longs.getMin();
      this.max = longs.getMax();
      this.sum = longs.getSum();
      this.count = longs.getCount();
    }
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

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(count);
    if (count > 0) {
      output.writeDouble(max);
      output.writeDouble(min);
      output.writeDouble(sum);
    }
  }

  @Override
  public void read(Kryo kryo, Input input) {
    this.count = input.readLong();
    if (count > 0) {
      this.max = input.readDouble();
      this.min = input.readDouble();
      this.sum = input.readDouble();
    }
  }
}
