package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.cpc.CpcSketch;

public class CpcSketchSerializer extends Serializer<CpcSketch> {

  private final RegistrationHelper helper = new RegistrationHelper();

  @Override
  public void write(Kryo kryo, Output output, CpcSketch sketch) {
    helper.checkByteArray(kryo);
    kryo.writeObject(output, sketch.toByteArray());
  }

  @Override
  public CpcSketch read(Kryo kryo, Input input, Class<? extends CpcSketch> type) {
    helper.checkByteArray(kryo);

    byte[] bytes = kryo.readObject(input, byte[].class);
    return CpcSketch.heapify(bytes);
  }
}
