package ai.whylabs.profile.stastistics;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class LongSummaryTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(LongSummary.class);

    val original = new LongSummary(0, 99, 99, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, LongSummary.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(LongSummary.class);

    val original = new LongSummary();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, LongSummary.class);
    assertEquals(original, roundTripObject);
  }
}