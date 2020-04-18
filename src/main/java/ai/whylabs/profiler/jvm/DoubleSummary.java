package ai.whylabs.profiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DoubleSummary {
    private double max;
    private double min;
    private double sum;
    private double m2;
    @Getter
    public long count;

    public DoubleSummary() {
        this(Double.MIN_VALUE, Double.MIN_VALUE, 0.0, 0.0, 0L);
    }

    public void update(double value) {
        if (value > max) max = value;
        if (value < min) min = value;
        count++;
        sum += value;
    }
}
