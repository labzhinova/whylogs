package com.whylabs.logging.core.statistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.whylabs.logging.core.data.InferredType.Type;
import lombok.val;
import org.testng.annotations.Test;

public class SchemaTrackerTest {
  @Test
  public void track_Nothing_ShouldReturnUnknown() {
    val tracker = new SchemaTracker();

    val inferredType = tracker.computeType();
    assertThat(inferredType.getType(), is(Type.UNKNOWN));
    assertThat(inferredType.getRatio(), is(0.0));
  }

  @Test
  public void track_VariousDataTypes_ShouldHaveCorrectCount() {
    val original = new SchemaTracker();

    original.track(10L);
    original.track(2L);
    assertThat(original.getCount(Type.INTEGRAL), is(2L));

    original.track("string");
    assertThat(original.getCount(Type.STRING), is(1L));

    original.track(2.0);
    assertThat(original.getCount(Type.FRACTIONAL), is(1L));

    original.track(true);
    original.track(false);
    assertThat(original.getCount(Type.BOOLEAN), is(2L));

    original.track(System.out);
    assertThat(original.getCount(Type.UNKNOWN), is(1L));
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

    val inferredType = tracker.computeType();
    assertThat(inferredType.getType(), is(Type.STRING));
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

    val inferredType = tracker.computeType();
    assertThat(inferredType.type, is(Type.FRACTIONAL));
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

    assertThat(tracker.computeType().getType(), is(Type.INTEGRAL));
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

    val inferredType = tracker.computeType();
    assertThat(inferredType.getType(), is(Type.FRACTIONAL));
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

    val inferredType = tracker.computeType();

    assertThat(inferredType.getType(), is(Type.STRING));
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
    val roundtrip = SchemaTracker.fromProtobuf(protoBuf.build());

    assertThat(protoBuf.build(), is(roundtrip.toProtobuf().build()));
    assertThat(roundtrip.getCount(Type.INTEGRAL), is(10L));
    assertThat(roundtrip.getCount(Type.STRING), is(100L));
  }
}
