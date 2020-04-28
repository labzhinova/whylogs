package com.whylabs.logging.core.statistics;

import static org.testng.Assert.assertEquals;

import com.whylabs.logging.core.statistics.schema.ColumnDataType;
import com.whylabs.logging.core.statistics.schema.SchemaTracker;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {

  @Test
  public void track_VariousDataTypes_ShouldHaveCorrectCount() {
    val original = new SchemaTracker();

    original.track(10L);
    original.track(2L);
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.INTEGRAL), 2L);

    original.track("string");
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.STRING), 1L);

    original.track(2.0);
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.FRACTIONAL), 1L);

    original.track(true);
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.BOOLEAN), 1L);

    original.track(System.out);
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.UNKNOWN), 1L);
  }
}
