package com.whylabs.logging.core.statistics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import lombok.val;
import org.testng.annotations.Test;

public class CountersTrackerTest {
  @Test
  public void kryo_RoundtripSerialization_ShouldSucceed() {
    val original = new CountersTracker();
    assertEquals(original.getCount(), 0);

    original.incrementCount();
    original.incrementCount();

    assertEquals(original.getCount(), 2);
    assertNull(original.getNullCount());
    assertNull(original.getTrueCount());

    original.incrementNull();
    assertEquals(original.getNullCount(), Long.valueOf(1));

    original.incrementTrue();
    assertEquals(original.getTrueCount(), Long.valueOf(1));
  }
}
