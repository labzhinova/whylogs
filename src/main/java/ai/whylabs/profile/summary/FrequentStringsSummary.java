package ai.whylabs.profile.summary;

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

  private static final FrequentStringsSummary EMPTY_SUMMARY =
      new FrequentStringsSummary(Collections.emptyList());
  List<String> items;

  public static FrequentStringsSummary fromStringSketch(ItemsSketch<String> sketch) {
    val frequentItems = sketch.getFrequentItems(ErrorType.NO_FALSE_NEGATIVES);

    if (frequentItems.length == 0) {
      return null;
    }
    List<String> result =
        Stream.of(frequentItems).map(ItemsSketch.Row::getItem).collect(Collectors.toList());

    return new FrequentStringsSummary(Collections.unmodifiableList(result));
  }

  public static FrequentStringsSummary empty() {
    return EMPTY_SUMMARY;
  }
}
