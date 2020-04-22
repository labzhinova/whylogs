package ai.whylabs.profile.statistics;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.ColumnDataType;
import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {

  @Test
  public void kryo_RoundtripSerialization_ShouldSucceed() {
    val kryo = new Kryo();
    kryo.register(SchemaTracker.class);

    val original = new SchemaTracker();

    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));

    original.track(10L);
    original.track(2L);
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.INTEGRAL), 2L);

    original.track("string");
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.STRING), 1L);

    original.track(2.0);
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.FRACTIONAL), 1L);

    original.track(true);
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.BOOLEAN), 1L);

    original.track(System.out);
    assertEquals(original, KryoUtils.doRoundTrip(kryo, original, SchemaTracker.class));
    assertEquals((long) original.getTypeCounts().get(ColumnDataType.UNKNOWN), 1L);
  }

}