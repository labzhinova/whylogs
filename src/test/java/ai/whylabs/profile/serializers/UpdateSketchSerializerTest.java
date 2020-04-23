package ai.whylabs.profile.serializers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.kryo.UpdateSketchSerializer;
import lombok.val;
import org.apache.datasketches.theta.UpdateSketch;
import org.testng.annotations.Test;

public class UpdateSketchSerializerTest extends KryoBaseTest<UpdateSketch> {

  public UpdateSketchSerializerTest() {
    super(new UpdateSketchSerializer(), UpdateSketch.class);
  }

  @Override
  public UpdateSketch createObject() {
    val sketch = UpdateSketch.builder().build();
    sketch.update("foo");
    sketch.update("bar");
    return sketch;
  }

  @Override
  public void verify(UpdateSketch original, UpdateSketch deserializedObject) {
    assertEquals(original.getTheta(), deserializedObject.getTheta());
    assertEquals(original.getEstimate(), deserializedObject.getEstimate());
    assertEquals(original.getRetainedEntries(), deserializedObject.getRetainedEntries());
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
