package ai.whylabs.profile.summary;

import lombok.Value;
import lombok.val;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

import java.util.stream.IntStream;

@Value
public class HistogramSummary {
    double start;
    double end;
    double width;
    long[] counts;

    public static HistogramSummary fromUpdateDoublesSketch(UpdateDoublesSketch sketch, double stddev) {
        val start = sketch.getMinValue() - stddev;
        val end = sketch.getMaxValue() + stddev;
        val width = (start + end) / 100.0;

        double[] splitPoints = IntStream.range(0, 100).boxed().map(idx -> start + idx * width).mapToDouble(Double::doubleValue).toArray();
        double[] pmf = sketch.getPMF(splitPoints);

        long[] counts = new long[pmf.length - 1];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = Math.round(pmf[i] * sketch.getN());
        }

        return new HistogramSummary(start, end, width, counts);
    }
}
