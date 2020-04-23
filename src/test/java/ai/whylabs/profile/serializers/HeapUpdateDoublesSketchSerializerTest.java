package ai.whylabs.profile.serializers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.kryo.HeapUpdateDoublesSketchSerializer;
import lombok.val;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.testng.annotations.Test;

public class HeapUpdateDoublesSketchSerializerTest extends KryoBaseTest<DoublesSketch> {

  public HeapUpdateDoublesSketchSerializerTest() {
    super(new HeapUpdateDoublesSketchSerializer(), DoublesSketch.class);
  }

  @Override
  public DoublesSketch createObject() {
    val sketch = DoublesSketch.builder().setK(256).build();
    sketch.update(1.0);
    sketch.update(2.0);
    return sketch;
  }

  @Override
  public void verify(DoublesSketch original, DoublesSketch deserializedObject) {
    assertEquals(original.getMaxValue(), deserializedObject.getMaxValue());
    assertEquals(original.getMinValue(), deserializedObject.getMinValue());
    assertEquals(original.getN(), deserializedObject.getN());
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
