package ai.whylabs.profile.summary;

import lombok.Value;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;


@Value
public class QuantilesSummary {
    double[] quantiles;

    public static QuantilesSummary fromUpdateDoublesSketch(UpdateDoublesSketch sketch) {
        return new QuantilesSummary(sketch.getQuantiles(100));
    }

}
