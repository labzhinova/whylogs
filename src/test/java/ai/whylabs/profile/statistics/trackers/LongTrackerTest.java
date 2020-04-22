package ai.whylabs.profile.statistics.trackers;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class LongTrackerTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(LongTracker.class);

    val original = new LongTracker(0, 99, 99, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, LongTracker.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(LongTracker.class);

    val original = new LongTracker();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, LongTracker.class);
    assertEquals(original, roundTripObject);
  }
}
