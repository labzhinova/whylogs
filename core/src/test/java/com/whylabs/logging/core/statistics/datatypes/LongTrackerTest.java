package com.whylabs.logging.core.statistics.datatypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.val;
import org.testng.annotations.Test;

public class LongTrackerTest {

  @Test
  public void update_BasicCase_ShouldBeCorrect() {
    val tracker = new LongTracker();
    tracker.update(1L);
    tracker.update(2L);
    tracker.update(3L);

    assertThat(tracker.getCount(), is(3L));
    assertThat(tracker.getMax(), is(3L));
    assertThat(tracker.getMin(), is(1L));
    assertThat(tracker.getMean(), is(2.0));
  }
}
