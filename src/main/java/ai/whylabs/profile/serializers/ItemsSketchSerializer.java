package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.ArrayOfStringsSerDe;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.memory.Memory;

public class ItemsSketchSerializer extends Serializer<ItemsSketch<String>> {

  private final RegistrationHelper helper = new RegistrationHelper();

  @Override
  public void write(Kryo kryo, Output output, ItemsSketch<String> sketch) {
    helper.checkByteArray(kryo);
    byte[] bytes = sketch.toByteArray(new ArrayOfStringsSerDe());
    kryo.writeObject(output, bytes);
  }

  @Override
  public ItemsSketch<String> read(Kryo kryo, Input input,
      Class<? extends ItemsSketch<String>> type) {
    helper.checkByteArray(kryo);

    byte[] bytes = kryo.readObject(input, byte[].class);
    Memory memory = Memory.wrap(bytes);
    return ItemsSketch.getInstance(memory, new ArrayOfStringsSerDe());
  }
}
