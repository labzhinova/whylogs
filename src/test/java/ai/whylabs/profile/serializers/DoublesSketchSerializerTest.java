package ai.whylabs.profile.serializers;

import static org.testng.Assert.assertEquals;

import lombok.val;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.testng.annotations.Test;

public class DoublesSketchSerializerTest extends KryoBaseTest<DoublesSketch> {

  public DoublesSketchSerializerTest() {
    super(new DoublesSketchSerializer(), DoublesSketch.class);
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
