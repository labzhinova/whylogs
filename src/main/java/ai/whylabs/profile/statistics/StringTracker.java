package ai.whylabs.profile.statistics;

import ai.whylabs.profile.serializers.kryo.ItemsSketchSerializer;
import ai.whylabs.profile.serializers.kryo.UpdateSketchSerializer;
import ai.whylabs.profile.serializers.kryo.helpers.SerializerRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.theta.UpdateSketch;

@Getter
public final class StringTracker implements KryoSerializable {

  private final transient SerializerRegistrationHelper serializerHelper;

  private long count;

  // sketches
  private final ItemsSketch<String> stringsSketch;
  private final UpdateSketch thetaSketch;

  public StringTracker() {
    this.serializerHelper = new SerializerRegistrationHelper(
        new UpdateSketchSerializer(),
        new ItemsSketchSerializer());

    this.count = 0L;
    this.stringsSketch = new ItemsSketch<>(32); // TODO: make this value configurable
    this.thetaSketch = UpdateSketch.builder().build();
  }

  public void update(String value) {
    if (value == null) {
      return;
    }

    count++;
    thetaSketch.update(value);
    stringsSketch.update(value);
  }

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(count);
    if (count == 0) {
      return;
    }

    serializerHelper.checkAndRegister(kryo);
    kryo.writeObject(output, stringsSketch);
    kryo.writeObject(output, thetaSketch);
  }

  @Override
  public void read(Kryo kryo, Input input) {

  }
}
