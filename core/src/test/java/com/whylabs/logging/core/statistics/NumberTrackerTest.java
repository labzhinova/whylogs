package com.whylabs.logging.core.statistics;

import static org.testng.Assert.assertEquals;

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
    assertEquals(Math.round(numberTracker.getThetaSketch().getEstimate()), 3L);
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
    assertEquals(Math.round(numberTracker.getThetaSketch().getEstimate()), 3L);
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
    assertEquals(Math.round(numberTracker.getThetaSketch().getEstimate()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getN()), 3L);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMaxValue()), 12);
    assertEquals(Math.round(numberTracker.getNumbersSketch().getMinValue()), 10);
  }
}
