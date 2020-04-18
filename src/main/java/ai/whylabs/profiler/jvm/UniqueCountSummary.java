package ai.whylabs.profiler.jvm;

import lombok.Value;
import org.apache.datasketches.cpc.CpcSketch;

@Value
public class UniqueCountSummary {
    double estimate;
    double upper;
    double lower;

    static UniqueCountSummary fromCpcSketch(CpcSketch sketch) {
        return new UniqueCountSummary(sketch.getEstimate(), sketch.getUpperBound(2), sketch.getLowerBound(2));
    }

}
