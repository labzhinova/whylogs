package ai.whylabs.profile.statistics;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class CountersTest {
  @Test
  public void kryo_RoundtripSerialization_ShouldSucceed() {
    val kryo = new Kryo();
    kryo.register(Counters.class);

    val original = new Counters();
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, Counters.class));

    original.incrementCount();
    original.incrementCount();

    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, Counters.class));

    original.incrementNull();
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, Counters.class));

    original.incrementTrue();
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, Counters.class));
  }
}
