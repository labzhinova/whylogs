package ai.whylabs.profile.summary;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class StandardDeviationSummary {

  private double sum;
  private double m2;
  private double mean;
  private long n;

  public StandardDeviationSummary() {
    this(0.0, 0.0, 0.0, 0L);
  }

  public void update(double value) {
    n++;
    sum += value;
    val delta = value - mean;
    mean += delta / n;
    m2 += delta * (value - mean);
  }

  public double stddev() {
    if (n < 2) {
      return Double.NaN;
    }
    return Math.sqrt(m2 / (n - 1.0));
  }
}
