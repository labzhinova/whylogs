package ai.whylabs.profile.stastistics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
public class LongSummary implements KryoSerializable {

  private long min;
  private long max;
  private long sum;
  @Getter
  public long count;

  public LongSummary() {
    this(Long.MAX_VALUE, Long.MIN_VALUE, 0L, 0L);
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
