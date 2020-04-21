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
public class LongTracker implements KryoSerializable {

  private long min;
  private long max;
  private long sum;
  private long count;

  public LongTracker() {
    reset();
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

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(count);
    if (count > 0) {
      output.writeLong(min);
      output.writeLong(max);
      output.writeLong(sum);
    }
  }

  @Override
  public void read(Kryo kryo, Input input) {
    this.count = input.readLong();
    if (this.count > 0) {
      this.min = input.readLong();
      this.max = input.readLong();
      this.sum = input.readLong();
    }
  }
}
