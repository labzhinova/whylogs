package com.whylabs.logging.core.summary;

import com.whylabs.logging.core.statistics.StringTracker;
import lombok.Value;
import lombok.val;

@Value
public class StringSummary {

  FrequentStringsSummary frequent;
  UniqueCountSummary uniqueness;

  public static StringSummary fromTracker(StringTracker tracker) {
    if (tracker == null) {
      return null;
    }

    if (tracker.getCount() == 0) {
      return null;
    }

    val uniqueness = UniqueCountSummary.fromSketch(tracker.getThetaSketch());

    // TODO: make this value configurable
    FrequentStringsSummary frequentStrings = null;
    if (uniqueness.getEstimate() < 100) {
      frequentStrings = FrequentStringsSummary.fromStringSketch(tracker.getStringsSketch());
    }

    return new StringSummary(frequentStrings, uniqueness);
  }
}
