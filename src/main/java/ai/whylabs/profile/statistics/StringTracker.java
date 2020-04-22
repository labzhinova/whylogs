package ai.whylabs.profile.statistics;

import ai.whylabs.profile.serializers.CpcSketchSerializer;
import ai.whylabs.profile.serializers.ItemsSketchSerializer;
import ai.whylabs.profile.serializers.helpers.SerializerRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;
import org.apache.datasketches.cpc.CpcSketch;
import org.apache.datasketches.frequencies.ItemsSketch;

@Getter
public final class StringTracker implements KryoSerializable {
  private final SerializerRegistrationHelper serializerHelper;

  private long count;

  // sketches
  private final ItemsSketch<String> stringsSketch;
  private final CpcSketch cpcSketch;

  private double lastUniqueEst;

  public StringTracker() {
    this.serializerHelper = new SerializerRegistrationHelper(
        new CpcSketchSerializer(),
        new ItemsSketchSerializer());

    this.count = 0L;
    this.stringsSketch = new ItemsSketch<>(128); // TODO: make this value configurable
    this.cpcSketch = new CpcSketch();
    this.lastUniqueEst = 0.0;
  }

  public void update(String value) {
    if (value == null) {
      return;
    }

    count++;
    cpcSketch.update(value);

    if (lastUniqueEst > 100) {
      // stop
      stringsSketch.reset();
    } else {
      lastUniqueEst = cpcSketch.getEstimate();
      stringsSketch.update(value);
    }
  }

  @Override
  public void write(Kryo kryo, Output output) {
    output.writeLong(count);
    if (count == 0) {
      return;
    }

    serializerHelper.checkAndRegister(kryo);
    kryo.writeObject(output, stringsSketch);
    kryo.writeObject(output, cpcSketch);
  }

  @Override
  public void read(Kryo kryo, Input input) {

  }
}
