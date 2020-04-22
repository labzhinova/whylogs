package ai.whylabs.profile.serializers;

import ai.whylabs.profile.serializers.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.Collections;
import java.util.List;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

public class HeapUpdateDoublesSketchSerializer extends ClassTaggedSerializer<UpdateDoublesSketch> {

  private final ClassRegistrationHelper helper = new ClassRegistrationHelper(byte[].class);

  @Override
  public void write(Kryo kryo, Output output, UpdateDoublesSketch sketch) {
    helper.checkAndRegister(kryo);
    kryo.writeObject(output, sketch.toByteArray());
  }

  @Override
  public UpdateDoublesSketch read(
      Kryo kryo, Input input, Class<? extends UpdateDoublesSketch> type) {
    helper.checkAndRegister(kryo);
    byte[] bytes = kryo.readObject(input, byte[].class);
    return UpdateDoublesSketch.heapify(Memory.wrap(bytes));
  }

  @Override
  public Class<?> getClassTag() {
    return UpdateDoublesSketch.class;
  }

  @Override
  public List<Class<?>> getAdditionalClassTags() {
    try {
      // this is a private class so we can't access the class directly
      Class<?> clazz = Class.forName("org.apache.datasketches.quantiles.HeapUpdateDoublesSketch");
      return Collections.singletonList(clazz);
    } catch (ClassNotFoundException e) {
      return Collections.emptyList();
    }
  }
}
