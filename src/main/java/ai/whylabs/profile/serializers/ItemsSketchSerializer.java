package ai.whylabs.profile.serializers;

import ai.whylabs.profile.serializers.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.ArrayOfStringsSerDe;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.memory.Memory;

public class ItemsSketchSerializer extends ClassTaggedSerializer<ItemsSketch<String>> {

  private final ClassRegistrationHelper helper = new ClassRegistrationHelper(byte[].class);

  @Override
  public void write(Kryo kryo, Output output, ItemsSketch<String> sketch) {
    helper.checkAndRegister(kryo);
    byte[] bytes = sketch.toByteArray(new ArrayOfStringsSerDe());
    kryo.writeObject(output, bytes);
  }

  @Override
  public ItemsSketch<String> read(
      Kryo kryo, Input input, Class<? extends ItemsSketch<String>> type) {
    helper.checkAndRegister(kryo);

    byte[] bytes = kryo.readObject(input, byte[].class);
    Memory memory = Memory.wrap(bytes);
    return ItemsSketch.getInstance(memory, new ArrayOfStringsSerDe());
  }

  @Override
  public Class<?> getClassTag() {
    return ItemsSketch.class;
  }
}
