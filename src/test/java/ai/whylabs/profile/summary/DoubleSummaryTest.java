package ai.whylabs.profile.summary;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class DoubleSummaryTest {

  @Test
  public void test_RoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(DoubleSummary.class);

    val original = new DoubleSummary(0.0, 99.0, 99.0, 2L);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleSummary.class);
    assertEquals(original, roundTripObject);
  }

  @Test
  public void test_RoundtripSerialization_ZeroCount() {
    val kryo = new Kryo();
    kryo.register(DoubleSummary.class);

    val original = new DoubleSummary();

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleSummary.class);
    assertEquals(original, roundTripObject);
  }
}