package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class VarianceTrackerTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(VarianceTracker.class);

    val original = new VarianceTracker(5.0, 1.0, 2.0, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, VarianceTracker.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(VarianceTracker.class);

    val original = new VarianceTracker();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, VarianceTracker.class);
    assertEquals(original, roundTripObject);
  }
}