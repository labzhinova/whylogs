package com.whylabs.logging.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.Test;

public class DatasetProfileTest {
  @Test
  public void merge_EmptyValidDatasetProfiles_EmptyResult() {
    final Instant now = Instant.now();
    val first = new DatasetProfile("test", now);
    val second = new DatasetProfile("test", now);

    final val result = first.merge(second);
    assertThat(result.getName(), is("test"));
    assertThat(result.getTimestamp(), is(now));
    assertThat(result.columns, is(anEmptyMap()));
  }

  @Test
  public void merge_DifferentColumns_ColumnsAreMerged() {
    final Instant now = Instant.now();
    val first = new DatasetProfile("test", now);
    first.track("col1", "value");
    val second = new DatasetProfile("test", now);
    second.track("col2", "value");

    final val result = first.merge(second);
    assertThat(result.getName(), is("test"));
    assertThat(result.getTimestamp(), is(now));
    assertThat(result.columns, aMapWithSize(2));
    assertThat(result.columns, hasKey("col1"));
    assertThat(result.columns, hasKey("col2"));

    // verify counters
    assertThat(result.columns.get("col1").getCounters().getCount(), is(1L));
    assertThat(result.columns.get("col2").getCounters().getCount(), is(1L));
  }

  @Test
  public void merge_SameColumns_ColumnsAreMerged() {
    final Instant now = Instant.now();
    val first = new DatasetProfile("test", now);
    first.track("col1", "value1");
    val second = new DatasetProfile("test", now);
    second.track("col1", "value1");
    second.track("col2", "value");

    final val result = first.merge(second);
    assertThat(result.getName(), is("test"));
    assertThat(result.getTimestamp(), is(now));
    assertThat(result.columns, aMapWithSize(2));
    assertThat(result.columns, hasKey("col1"));
    assertThat(result.columns, hasKey("col2"));

    // verify counters
    assertThat(result.columns.get("col1").getCounters().getCount(), is(2L));
    assertThat(result.columns.get("col2").getCounters().getCount(), is(1L));
  }

  @Test
  public void protobuf_RoundTripSerialization_Success() {
    val original = new DatasetProfile("test", Instant.now());
    original.track("col1", "value");
    original.track("col2", "value");

    final val msg = original.toProtobuf().build();
    final val roundTrip = DatasetProfile.fromProtobuf(msg);

    assertThat(roundTrip.getName(), is("test"));
    assertThat(roundTrip.columns, aMapWithSize(2));
    assertThat(roundTrip.columns.get("col1").getCounters().getCount(), is(1L));
    assertThat(roundTrip.columns.get("col2").getCounters().getCount(), is(1L));
  }

  @Test
  public void javaSerialization_RoundTrip_Success() {
    val time = Instant.now();
    val original = new DatasetProfile("test", time);
    original.track("col1", "value");
    original.track("col1", 1);
    original.track("col2", "value");

    val clone = SerializationUtils.clone(original);
    assertThat(clone.getName(), is("test"));
    assertThat(clone.getTimestamp(), is(time));
    assertThat(clone.columns, aMapWithSize(2));
  }
}
