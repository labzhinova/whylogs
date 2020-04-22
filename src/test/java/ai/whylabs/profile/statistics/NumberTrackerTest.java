package ai.whylabs.profile.statistics;

import static org.testng.Assert.assertEquals;

import ai.whylabs.profile.serializers.KryoUtils;
import com.esotericsoftware.kryo.Kryo;
import lombok.val;
import org.testng.annotations.Test;

public class NumberTrackerTest {

  @Test
  public void track_LongValue_ShouldNotIncreaseDoubleCount() {
    val numberTracker = new NumberTracker();
    numberTracker.track(10L);
    numberTracker.track(11L);
    numberTracker.track(12);

    assertEquals(numberTracker.getLongs().getCount(), 3L);
    assertEquals(numberTracker.getDoubles().getCount(), 0L);
    assertEquals(numberTracker.getStddev().value(), 1.0);
    assertEquals(Math.round(numberTracker.getCpcSketch().getEstimate()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getN()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMaxValue()), 12);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMinValue()), 10);
  }

  @Test
  public void track_DoubleValue_ShouldNotIncreaseLongCount() {
    val numberTracker = new NumberTracker();
    numberTracker.track(10.0);
    numberTracker.track(11.0);
    numberTracker.track(12.0);

    assertEquals(numberTracker.getLongs().getCount(), 0L);
    assertEquals(numberTracker.getDoubles().getCount(), 3L);
    assertEquals(numberTracker.getStddev().value(), 1.0);
    assertEquals(Math.round(numberTracker.getCpcSketch().getEstimate()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getN()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMaxValue()), 12);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMinValue()), 10);
  }

  @Test
  public void track_DoubleValueAfterLongValue_ShouldResetLongsTracker() {
    val numberTracker = new NumberTracker();
    numberTracker.track(10L);
    numberTracker.track(11L);
    assertEquals(numberTracker.getLongs().getCount(), 2L);
    assertEquals(numberTracker.getDoubles().getCount(), 0L);

    // instead of Long, we got a double value here
    numberTracker.track(12.0);

    assertEquals(numberTracker.getLongs().getCount(), 0L);
    assertEquals(numberTracker.getDoubles().getCount(), 3L);
    assertEquals(numberTracker.getStddev().value(), 1.0);
    assertEquals(Math.round(numberTracker.getCpcSketch().getEstimate()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getN()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMaxValue()), 12);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMinValue()), 10);
  }

  @Test
  public void kryo_RoundtripSerialization_ShouldSucceed() {
    val kryo = new Kryo();
    kryo.register(NumberTracker.class);

    val original = new NumberTracker();
    original.track(10.0);
    original.track(11.0);
    original.track(12.0);

    val roundTripObject = KryoUtils.doRoundTrip(kryo, original, NumberTracker.class);
    assertEquals(original.getLongs(), roundTripObject.getLongs());
    assertEquals(original.getDoubles(), roundTripObject.getDoubles());
    assertEquals(original.getStddev(), roundTripObject.getStddev());
  }

  @Test
  public void kryo_RoundtripSerializationEmptyObject_ShouldSucceed() {
    val kryo = new Kryo();
    kryo.register(NumberTracker.class);

    val original = new NumberTracker();

    assertEquals(original.getLongs().getCount(), 0);
    assertEquals(original.getDoubles().getCount(), 0);
    assertEquals(original.getStddev().getN(), 0);
  }
}
