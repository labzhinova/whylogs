package ai.whylabs.profile.summary;

import java.util.stream.IntStream;
import lombok.Value;
import lombok.val;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

@Value
public class HistogramSummary {

  double start;
  double end;
  double width;
  long[] counts;

  public static HistogramSummary fromUpdateDoublesSketch(
      UpdateDoublesSketch sketch) {
    val n = sketch.getN();
    double start = sketch.getMinValue();
    double end = sketch.getMaxValue();

    // try to be smart here. We don't really have a "histogram"
    // if there are too few data points or there's no band
    if (n < 2 || start == end) {
      return new HistogramSummary(start, end, 0, new long[]{n});
    }

    int numberOfBuckets = (int) Math.min(Math.ceil(n / 4.0), 100);
    val width = (end - start) / (numberOfBuckets * 1.0);
    double[] splitPoints =
        IntStream.range(0, numberOfBuckets)
            .boxed()
            .map(idx -> start + idx * width)
            .mapToDouble(Double::doubleValue)
            .toArray();
    double[] pmf = sketch.getPMF(splitPoints);

    long[] counts = new long[pmf.length - 1];
    for (int i = 0; i < counts.length; i++) {
      counts[i] = Math.round(pmf[i] * sketch.getN());
    }

    return new HistogramSummary(start, end, width, counts);
  }
}
