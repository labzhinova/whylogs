package ai.whylabs.profile.summary;

import lombok.Value;
import org.apache.datasketches.theta.UpdateSketch;

@Value
public class UniqueCountSummary {

  double estimate;
  double upper;
  double lower;

  public static UniqueCountSummary fromSketch(UpdateSketch sketch) {
    return new UniqueCountSummary(
        sketch.getEstimate(),
        sketch.getUpperBound(1),
        sketch.getLowerBound(1));
  }
}
