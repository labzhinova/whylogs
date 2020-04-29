package com.whylabs.logging.core.statistics;

import static org.testng.Assert.assertEquals;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.statistics.schema.SchemaTracker;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {

  @Test
  public void track_VariousDataTypes_ShouldHaveCorrectCount() {
    val original = new SchemaTracker();

    original.track(10L);
    original.track(2L);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.INTEGRAL), 2L);

    original.track("string");
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.STRING), 1L);

    original.track(2.0);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.FRACTIONAL), 1L);

    original.track(true);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.BOOLEAN), 1L);

    original.track(System.out);
    assertEquals((long) original.getTypeCounts().get(InferredType.Type.UNKNOWN), 1L);
  }
}
