package ai.whylabs.profile.statistics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode
@Getter
@FieldDefaults(level=AccessLevel.PRIVATE)
public class Counters implements KryoSerializable {
  long count;

  // Nullable values
  Long trueCount;
  Long nullCount;

  public void incrementCount() {
    count++;
  }

  public void incrementTrue() {
    trueCount = (trueCount == null) ? 1: trueCount + 1;
  }

  public void incrementNull() {
    nullCount = (nullCount == null) ? 1 : nullCount + 1;
  }

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(count);

    if (count > 0) {
      kryo.writeObjectOrNull(output, trueCount, Long.class);
      kryo.writeObjectOrNull(output, nullCount, Long.class);
    }
  }

  @Override
  public void read(Kryo kryo, Input input) {
    count = input.readLong();
    if (count > 0) {
      trueCount = kryo.readObjectOrNull(input, Long.class);
      nullCount = kryo.readObjectOrNull(input, Long.class);
    }
  }
}
