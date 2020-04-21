package ai.whylabs.profile.summary;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DoubleSummary implements KryoSerializable {

  private double min;
  private double max;
  private double sum;
  @Getter
  public long count;

  public DoubleSummary() {
    this(Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0L);
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
