package ai.whylabs.profile.serializers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.kryo.ItemsSketchSerializer;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.testng.annotations.Test;

@SuppressWarnings({"rawtypes"})
public class ItemsSketchStringTypeAdapterTest extends KryoBaseTest<ItemsSketch> {

  static Kryo kryo = new Kryo();

  static {
    kryo.register(ItemsSketch.class, new ItemsSketchSerializer());
  }

  public ItemsSketchStringTypeAdapterTest() {
    super(new ItemsSketchSerializer(), ItemsSketch.class);
  }

  @Override
  public ItemsSketch createObject() {
    val sketch = new ItemsSketch<>(128);
    sketch.update("foo");
    sketch.update("bar");

    return sketch;
  }

  @Override
  public void verify(ItemsSketch original, ItemsSketch result) {
    assertEquals(result.getNumActiveItems(), 2);
  }

  @Test
  public void testRoundtrip() {
    this.runFullSerialization();
  }

  @Test
  public void testNull() {
    this.runWithNull();
  }
}
