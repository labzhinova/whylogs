package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
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

  @Test
  public void kryo_RoundTripSerializationWithValues_ShouldWork() {
    val kryo = new Kryo();
    kryo.register(DoubleTracker.class);

    val original = new DoubleTracker(0.0, 99.0, 99.0, 2L);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleTracker.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void kryo_RoundTripSerializationWithEmptyTracker_ShouldWork() {
    val kryo = new Kryo();
    kryo.register(DoubleTracker.class);

    val original = new DoubleTracker();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleTracker.class);
    assertEquals(original, roundTripObject);
  }
}