package ai.whylabs.profile;

import ai.whylabs.profile.summary.DoubleSummary;
import ai.whylabs.profile.summary.FrequentStringsSummary;
import ai.whylabs.profile.summary.HistogramSummary;
import ai.whylabs.profile.summary.LongSummary;
import ai.whylabs.profile.summary.QuantilesSummary;
import ai.whylabs.profile.summary.StandardDeviationSummary;
import ai.whylabs.profile.summary.UniqueCountSummary;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.datasketches.cpc.CpcSketch;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ColumnProfile {

  private static final Pattern FRACTIONAL = Pattern.compile("^[-+]?( )?\\d+([.]\\d+)$");
  private static final Pattern INTEGRAL = Pattern.compile("^[-+]?( )?\\d+$");
  private static final Pattern BOOLEAN = Pattern.compile("^(?i)(true|false)$");

  final String columnName;
  final Map<ColumnDataType, Long> typeCounts;
  final CpcSketch cpcSketch;
  final ItemsSketch<String> stringsSketch;
  final UpdateDoublesSketch numbersSketch;

  final LongSummary longSummary;
  final DoubleSummary doubleSummary;
  final StandardDeviationSummary stddevSummary;

  long totalCount;
  long trueCount;
  long nullCount;

  public ColumnProfile(String columnName) {
    this(
        columnName,
        new EnumMap<>(ColumnDataType.class),
        new CpcSketch(),
        new ItemsSketch<>(128),
        DoublesSketch.builder().setK(256).build(),
        new LongSummary(),
        new DoubleSummary(),
        new StandardDeviationSummary(),
        0L,
        0L,
        0L);
  }

  private static Object normalizeType(Object data) {
    if (data == null) {
      return null;
    }

    if (data instanceof String) {
      val strData = (String) data;
      if (INTEGRAL.matcher(strData).matches()) {
        return Long.parseLong(strData);
      }
      if (FRACTIONAL.matcher(strData).matches()) {
        return Double.parseDouble(strData);
      }
      if (BOOLEAN.matcher(strData).matches()) {
        return Boolean.parseBoolean(strData);
      }
    }

    if (data instanceof Double || data instanceof Float) {
      return ((Number) data).doubleValue();
    }

    if (data instanceof Integer || data instanceof Long || data instanceof Short) {
      return ((Number) data).longValue();
    }
    return data;
  }

  private static ColumnDataType toEnumType(Object data) {
    if (data == null) {
      return ColumnDataType.NULL;
    }

    if (data instanceof String) {
      return ColumnDataType.STRING;
    }

    if (data instanceof Long) {
      return ColumnDataType.INTEGRAL;
    }

    if (data instanceof Double) {
      return ColumnDataType.FRACTIONAL;
    }

    if (data instanceof Boolean) {
      return ColumnDataType.BOOLEAN;
    }

    return ColumnDataType.UNKNOWN;
  }

  public void track(Object value) {
    val normalizedData = normalizeType(value);
    if (normalizedData == null) {
      trackNull();
    } else if (normalizedData instanceof Long) {
      track((Long) normalizedData);
    } else if (normalizedData instanceof Double) {
      track((Double) normalizedData);
    } else if (normalizedData instanceof Boolean) {
      track((Boolean) normalizedData);
    } else if (normalizedData instanceof String) {
      track((String) normalizedData);
    }

    addTypeCount(toEnumType(normalizedData));
    totalCount++;
  }

  private void addTypeCount(ColumnDataType dataType) {
    this.typeCounts.compute(
        dataType, (type, existingValue) -> existingValue == null ? 1L : existingValue++);
  }

  private void trackNull() {
    nullCount++;
  }

  private void track(Boolean flag) {
    if (flag) {
      trueCount++;
    }
  }

  private void track(Double value) {
    doubleSummary.update(value);
    cpcSketch.update(value);
    numbersSketch.update(value);
    stddevSummary.update(value);
  }

  private void track(Long value) {
    doubleSummary.update(value);
    longSummary.update(value);
    cpcSketch.update(value);
    numbersSketch.update(value.doubleValue());
    stddevSummary.update(value);
  }

  private void track(String text) {
    cpcSketch.update(text);
    stringsSketch.update(text);
  }

  public InterpretableColumnStatistics toInterpretableStatistics() {
    return InterpretableColumnStatistics.builder()
        .totalCount(totalCount)
        .typeCounts(typeCounts)
        .nullCount(nullCount)
        .trueCount((trueCount == 0L) ? null : trueCount)
        .longSummary((longSummary.count == 0L) ? null : longSummary)
        .doubleSummary((doubleSummary.count == 0L) ? null : doubleSummary)
        .uniqueCountSummary(UniqueCountSummary.fromCpcSketch(cpcSketch))
        .quantilesSummary(
            (numbersSketch.getN() == 0L)
                ? null
                : QuantilesSummary.fromUpdateDoublesSketch(numbersSketch))
        .histogramSummary(
            (numbersSketch.getN() > 0L && numbersSketch.getMaxValue() > numbersSketch.getMinValue())
                ? HistogramSummary.fromUpdateDoublesSketch(numbersSketch, stddevSummary.stddev())
                : null)
        .frequentStringsSummary(
            cpcSketch.getEstimate() < 100
                ? FrequentStringsSummary.fromStringSketch(stringsSketch)
                : FrequentStringsSummary.empty())
        .build();
  }
}
