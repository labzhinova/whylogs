package ai.whylabs.profile.statistics.trackers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.val;

@EqualsAndHashCode
@AllArgsConstructor
public class StandardDeviationTracker implements KryoSerializable {

  private double sum;
  private double m2;
  private double mean;
  private long n;

  public StandardDeviationTracker() {
    this(0.0, 0.0, 0.0, 0L);
  }

  public void update(double value) {
    n++;
    sum += value;
    val delta = value - mean;
    mean += delta / n;
    m2 += delta * (value - mean);
  }

  public double stddev() {
    if (n < 2) {
      return Double.NaN;
    }
    return Math.sqrt(m2 / (n - 1.0));
  }

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(n);
    if (n > 0) {
      output.writeDouble(sum);
      output.writeDouble(m2);
      output.writeDouble(mean);
    }
  }

  @Override
  public void read(Kryo kryo, Input input) {
    n = input.readLong();
    if (n > 0) {
      sum = input.readDouble();
      m2 = input.readDouble();
      mean = input.readDouble();
    }
  }
}
