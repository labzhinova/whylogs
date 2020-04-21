package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

public class DoublesSketchSerializer extends Serializer<DoublesSketch> {

  private final RegistrationHelper helper = new RegistrationHelper();

  @Override
  public void write(Kryo kryo, Output output, DoublesSketch sketch) {
    helper.checkByteArray(kryo);
    kryo.writeObject(output, sketch.toByteArray());
  }

  @Override
  public DoublesSketch read(Kryo kryo, Input input, Class<? extends DoublesSketch> type) {
    helper.checkByteArray(kryo);
    byte[] bytes = kryo.readObject(input, byte[].class);
    return UpdateDoublesSketch.wrap(Memory.wrap(bytes));
  }

}
