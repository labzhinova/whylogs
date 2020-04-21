package ai.whylabs.profile.serializers;

import lombok.val;
import org.apache.datasketches.cpc.CpcSketch;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CpcSketchSerializerTest extends KryoBaseTest<CpcSketch> {

  public CpcSketchSerializerTest() {
    super(new CpcSketchSerializer(), CpcSketch.class);
  }

  @Override
  public CpcSketch createObject() {
    val sketch = new CpcSketch();
    sketch.update("foo");
    sketch.update("bar");
    return sketch;
  }

  @Override
  public void verify(CpcSketch original, CpcSketch deserializedObject) {
    Assert.assertEquals(original.getEstimate(), deserializedObject.getEstimate());
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
