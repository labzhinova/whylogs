package ai.whylabs.profile.serializers.kryo;

import ai.whylabs.profile.serializers.kryo.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.Collections;
import java.util.List;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.UpdateSketch;

public class UpdateSketchSerializer extends ClassTaggedSerializer<UpdateSketch> {

  private final ClassRegistrationHelper helper = new ClassRegistrationHelper(byte[].class);

  @Override
  public void write(Kryo kryo, Output output, UpdateSketch sketch) {
    helper.checkAndRegister(kryo);
    kryo.writeObject(output, sketch.toByteArray());
  }

  @Override
  public UpdateSketch read(Kryo kryo, Input input, Class<? extends UpdateSketch> type) {
    helper.checkAndRegister(kryo);

    byte[] bytes = kryo.readObject(input, byte[].class);
    return UpdateSketch.heapify(Memory.wrap(bytes));
  }

  @Override
  public Class<?> getClassTag() {
    return UpdateSketch.class;
  }

  @Override
  public List<Class<?>> getAdditionalClassTags() {
    try {
      // this is a private class so we can't access the class directly
      Class<?> clazz = Class.forName("org.apache.datasketches.theta.HeapQuickSelectSketch");
      return Collections.singletonList(clazz);
    } catch (ClassNotFoundException e) {
      return Collections.emptyList();
    }
  }
}
