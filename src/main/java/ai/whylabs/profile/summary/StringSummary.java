package ai.whylabs.profile.summary;

import ai.whylabs.profile.statistics.StringTracker;
import lombok.Value;

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

    UniqueCountSummary uniqueness = UniqueCountSummary
        .fromCpcSketch(tracker.getCpcSketch());

    // TODO: make this value configurable
    FrequentStringsSummary frequentStrings = null;
    if (uniqueness.getEstimate() < 100) {
      frequentStrings = FrequentStringsSummary
          .fromStringSketch(tracker.getStringsSketch());
    }

    return new StringSummary(frequentStrings, uniqueness);
  }
}
