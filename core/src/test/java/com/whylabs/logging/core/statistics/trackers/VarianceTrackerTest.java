package com.whylabs.logging.core.statistics.trackers;

import static org.testng.Assert.assertEquals;

import lombok.val;
import org.testng.annotations.Test;

public class VarianceTrackerTest {

  @Test
  public void update_BasicCases_ShouldBeCorrect() {
    val original = new VarianceTracker();
    original.update(1.0);
    original.update(2.0);
    original.update(3.0);

    assertEquals(original.getMean(), 2.0);
    assertEquals(original.getCount(), 3);
  }
}
