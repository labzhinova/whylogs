package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class DoubleTrackerTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(DoubleTracker.class);

    val original = new DoubleTracker(0.0, 99.0, 99.0, 2L);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleTracker.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(DoubleTracker.class);

    val original = new DoubleTracker();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleTracker.class);
    assertEquals(original, roundTripObject);
  }
}