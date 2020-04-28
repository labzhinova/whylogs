package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import lombok.val;
import org.testng.annotations.Test;

public class LongTrackerTest {

  @Test
  public void update_BasicCase_ShouldBeCorrect() {
    val tracker = new LongTracker();
    tracker.update(1L);
    tracker.update(2L);
    tracker.update(3L);

    assertEquals(tracker.getCount(), 3);
    assertEquals(tracker.getMax(), 3L);
    assertEquals(tracker.getMin(), 1L);
    assertEquals(tracker.getMean(), 2.0);
  }
}
