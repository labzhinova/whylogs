package ai.whylabs.profile.summary;

import ai.whylabs.profile.statistics.NumberTracker;
import lombok.Value;
import lombok.val;

@Value
public class NumberSummary {

  long count;
  Double stddev;
  Double min;
  Double max;
  Double mean;
  HistogramSummary histogram;
  UniqueCountSummary uniqueCount;

  public static NumberSummary fromNumberTracker(NumberTracker numberTracker) {
    long count = numberTracker.getStddev().getN();

    if (count == 0) {
      return null;
    }

    Double stddev = numberTracker.getStddev().value();
    double mean, min, max;
    if (numberTracker.getDoubles().getCount() > 0) {
      mean = numberTracker.getDoubles().getMean();
      min = numberTracker.getDoubles().getMin();
      max = numberTracker.getDoubles().getMax();
    } else {
      mean = numberTracker.getLongs().getMean();
      min = (double) numberTracker.getLongs().getMin();
      max = (double) numberTracker.getLongs().getMax();
    }

    val histogram = HistogramSummary.fromUpdateDoublesSketch(numberTracker.getNumbersSketch());
    val uniqueCount = UniqueCountSummary.fromCpcSketch(numberTracker.getCpcSketch());

    return new NumberSummary(count, stddev, min, max, mean, histogram, uniqueCount);
  }
}
