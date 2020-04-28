package com.whylabs.logging.core.summary;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Value;
import lombok.val;
import org.apache.datasketches.frequencies.ErrorType;
import org.apache.datasketches.frequencies.ItemsSketch;

@Value
public class FrequentStringsSummary {

  @Value
  public static class ItemFrequency {

    String value;
    double estimate;
  }

  private static final FrequentStringsSummary EMPTY_SUMMARY =
      new FrequentStringsSummary(Collections.emptyList());

  List<ItemFrequency> items;

  public static FrequentStringsSummary fromStringSketch(ItemsSketch<String> sketch) {
    val frequentItems = sketch.getFrequentItems(ErrorType.NO_FALSE_NEGATIVES);

    if (frequentItems.length == 0) {
      return null;
    }
    val result =
        Stream.of(frequentItems)
            .map(row -> new ItemFrequency(row.getItem(), row.getEstimate()))
            .collect(Collectors.toList());

    return new FrequentStringsSummary(Collections.unmodifiableList(result));
  }
}
