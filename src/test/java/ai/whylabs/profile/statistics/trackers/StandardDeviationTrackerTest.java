package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class StandardDeviationTrackerTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(StandardDeviationTracker.class);

    val original = new StandardDeviationTracker(5.0, 1.0, 2.0, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, StandardDeviationTracker.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(StandardDeviationTracker.class);

    val original = new StandardDeviationTracker();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, StandardDeviationTracker.class);
    assertEquals(original, roundTripObject);
  }
}