package com.whylabs.logging.core.statistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.val;
import org.testng.annotations.Test;

public class CountersTrackerTest {
  @Test
  public void counters_SimpleTracking() {
    val original = new CountersTracker();
    assertThat(original.getCount(), is(0L));

    original.incrementCount();
    original.incrementCount();

    assertThat(original.getCount(), is(2L));
    assertThat(original.getNullCount(), is(0L));
    assertThat(original.getTrueCount(), is(0L));

    original.incrementNull();
    assertThat(original.getNullCount(), is(1L));

    original.incrementTrue();
    assertThat(original.getTrueCount(), is(1L));
  }
}
