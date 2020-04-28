package com.whylabs.logging.core.statistics.trackers;

import static org.testng.Assert.assertEquals;

import lombok.val;
import org.testng.annotations.Test;

public class DoubleTrackerTest {

  @Test
  public void track_DoubleValues_ShouldReflectMinAndMax() {
    val tracker = new DoubleTracker();
    tracker.update(1.0);
    tracker.update(2.0);
    tracker.update(3.0);

    assertEquals(tracker.getCount(), 3);
    assertEquals(tracker.getMin(), 1.0);
    assertEquals(tracker.getMax(), 3.0);
    assertEquals(tracker.getMean(), 2.0);
  }
}
