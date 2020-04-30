package com.whylabs.logging.core.statistics;

import static org.testng.Assert.assertEquals;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import java.util.Collections;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {
  @Test
  public void track_Nothing_ShouldReturnUnknown() {
    val tracker = new SchemaTracker();

    assertEquals(tracker.getType(), Type.UNKNOWN);
    assertEquals(tracker.getTypeCounts(), Collections.emptyMap());
  }

  @Test
  public void track_VariousDataTypes_ShouldHaveCorrectCount() {
    val original = new SchemaTracker();

    original.track(10L);
    original.track(2L);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.INTEGRAL.getNumber()), 2L);

    original.track("string");
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.STRING.getNumber()), 1L);

    original.track(2.0);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.FRACTIONAL.getNumber()), 1L);

    original.track(true);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.BOOLEAN.getNumber()), 1L);

    original.track(System.out);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.UNKNOWN.getNumber()), 1L);
  }
}
