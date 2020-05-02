package com.whylabs.logging.core.statistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.SchemaMessage;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {
  @Test
  public void track_Nothing_ShouldReturnUnknown() {
    val tracker = new SchemaTracker();

    assertEquals(tracker.getType(), Type.UNKNOWN);
    assertThat(tracker.getType(), is(Type.UNKNOWN));
    assertThat(tracker.getTypeCounts(), is(anEmptyMap()));
  }

  @Test
  public void track_VariousDataTypes_ShouldHaveCorrectCount() {
    val original = new SchemaTracker();

    original.track(10L);
    original.track(2L);
    val typeCounts = original.getTypeCounts();
    assertThat(typeCounts.get(InferredType.Type.INTEGRAL.getNumber()), is(2L));

    original.track("string");
    assertThat(typeCounts.get(InferredType.Type.STRING.getNumber()), is(1L));

    original.track(2.0);
    assertThat(typeCounts.get(InferredType.Type.FRACTIONAL.getNumber()), is(1L));

    original.track(true);
    original.track(false);
    assertThat(typeCounts.get(InferredType.Type.BOOLEAN.getNumber()), is(2L));

    original.track(System.out);
    assertThat(typeCounts.get(InferredType.Type.UNKNOWN.getNumber()), is(1L));
  }

  @Test
  public void track_Over70PercentStringData_ShouldInferStringType() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 10; i++) {
      tracker.track(1L);
    }

    for (int i = 0; i < 71; i++) {
      tracker.track("stringdata");
    }

    assertThat(tracker.getOrComputeType().getType(), is(Type.STRING));
  }

  @Test
  public void track_MajorityDoubleData_ShouldInferFractionalType() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 50; i++) {
      tracker.track(0.1);
    }

    for (int i = 0; i < 30; i++) {
      tracker.track("stringdata");
    }

    for (int i = 0; i < 20; i++) {
      tracker.track(System.out);
    }

    assertThat(tracker.getOrComputeType().getType(), is(Type.FRACTIONAL));
  }

  @Test
  public void track_MajorityIntegerAndLongData_ShouldInferIntegralType() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 50; i++) {
      tracker.track(1L);
    }

    for (int i = 0; i < 30; i++) {
      tracker.track("stringdata");
    }

    for (int i = 0; i < 20; i++) {
      tracker.track(System.out);
    }
    assertThat(tracker.getOrComputeType().getType(), is(Type.INTEGRAL));
  }

  @Test
  public void track_DoubleAndLong_CoercedToFractional() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 50; i++) {
      tracker.track(1L);
    }

    for (int i = 0; i < 50; i++) {
      tracker.track(0.0);
    }

    for (int i = 0; i < 10; i++) {
      tracker.track("stringdata");
    }

    assertThat(tracker.getOrComputeType().getType(), is(Type.FRACTIONAL));
  }

  @Test
  public void track_AllTypesEqual_CoercedToString() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 20; i++) {
      tracker.track(1L);
    }

    for (int i = 0; i < 29; i++) {
      tracker.track(0.0);
    }

    for (int i = 0; i < 50; i++) {
      tracker.track("stringdata");
    }

    assertThat(tracker.getOrComputeType().getType(), is(Type.STRING));
  }


  @Test
  public void serialization_RoundTrip_ShouldMatch() {
    val tracker = new SchemaTracker();

    for (int i = 0; i < 10; i++) {
      tracker.track(1L);
    }

    for (int i = 0; i < 100; i++) {
      tracker.track("stringdata");
    }

    val protoBuf = tracker.toProtobuf();
    val roundtrip = SchemaTracker.fromProtobuf(protoBuf);

    assertThat(protoBuf, is(roundtrip.toProtobuf()));
    assertThat(roundtrip.getTypeCounts().get(Type.INTEGRAL_VALUE), is(10L));
    assertThat(roundtrip.getTypeCounts().get(Type.STRING_VALUE), is(100L));
  }

}
