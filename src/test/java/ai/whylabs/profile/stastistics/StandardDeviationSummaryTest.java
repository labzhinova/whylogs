package ai.whylabs.profile.stastistics;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class StandardDeviationSummaryTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(StandardDeviationSummary.class);

    val original = new StandardDeviationSummary(5.0, 1.0, 2.0, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, StandardDeviationSummary.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(StandardDeviationSummary.class);

    val original = new StandardDeviationSummary();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, StandardDeviationSummary.class);
    assertEquals(original, roundTripObject);
  }
}