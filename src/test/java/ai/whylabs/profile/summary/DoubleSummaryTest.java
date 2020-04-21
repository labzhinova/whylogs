package ai.whylabs.profile.summary;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.SneakyThrows;
import lombok.val;
import org.testng.annotations.Test;

public class DoubleSummaryTest {

  @SneakyThrows
  @Test
  public void testRoundtripSerialization() {
    val kryo = new Kryo();
    kryo.register(DoubleSummary.class);

    val original = new DoubleSummary(0.0, 99.0, 99.0, 2);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, DoubleSummary.class);

    assertEquals(original, roundTripObject);
  }

}