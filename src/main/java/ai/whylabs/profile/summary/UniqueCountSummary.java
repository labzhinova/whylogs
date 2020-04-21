package ai.whylabs.profile.summary;

import lombok.Value;
import org.apache.datasketches.cpc.CpcSketch;

@Value
public class UniqueCountSummary {

  double estimate;
  double upper;
  double lower;

  public static UniqueCountSummary fromCpcSketch(CpcSketch sketch) {
    return new UniqueCountSummary(
        sketch.getEstimate(), sketch.getUpperBound(2), sketch.getLowerBound(2));
  }
}
