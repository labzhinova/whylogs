package ai.whylabs.profile.serializers;

import ai.whylabs.profile.serializers.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.cpc.CpcSketch;

public class CpcSketchSerializer extends ClassTaggedSerializer<CpcSketch> {

  private final ClassRegistrationHelper helper = new ClassRegistrationHelper(byte[].class);

  @Override
  public void write(Kryo kryo, Output output, CpcSketch sketch) {
    helper.checkAndRegister(kryo);
    kryo.writeObject(output, sketch.toByteArray());
  }

  @Override
  public CpcSketch read(Kryo kryo, Input input, Class<? extends CpcSketch> type) {
    helper.checkAndRegister(kryo);

    byte[] bytes = kryo.readObject(input, byte[].class);
    return CpcSketch.heapify(bytes);
  }

  @Override
  public Class<?> getClassTag() {
    return CpcSketch.class;
  }
}
