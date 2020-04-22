package ai.whylabs.profile;

import ai.whylabs.profile.statistics.NumberTracker;
import ai.whylabs.profile.summary.FrequentStringsSummary;
import ai.whylabs.profile.summary.NumberSummary;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.datasketches.frequencies.ItemsSketch;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ColumnProfile {

  private static final Pattern FRACTIONAL = Pattern.compile("^[-+]?( )?\\d+([.]\\d+)$");
  private static final Pattern INTEGRAL = Pattern.compile("^[-+]?( )?\\d+$");
  private static final Pattern BOOLEAN = Pattern.compile("^(?i)(true|false)$");

  final String columnName;
  final Map<ColumnDataType, Long> typeCounts;
  final ItemsSketch<String> stringsSketch;
  final NumberTracker numberTracker;

  long totalCount;
  long trueCount;
  long nullCount;

  public ColumnProfile(String columnName) {
    this(
        columnName,
        new EnumMap<>(ColumnDataType.class),
        new ItemsSketch<>(128),
        new NumberTracker(),
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
    } else if (normalizedData instanceof Number) {
      numberTracker.track((Number) normalizedData);
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
        dataType,
        (type, existingValue) -> existingValue == null ? 1L : existingValue + 1);
  }

  private void trackNull() {
    nullCount++;
  }

  private void track(Boolean flag) {
    if (flag) {
      trueCount++;
    }
  }

  private void track(String text) {
    stringsSketch.update(text);
  }

  public InterpretableColumnStatistics toInterpretableStatistics() {
    val cpcSketch = numberTracker.getCpcSketch();
    return InterpretableColumnStatistics.builder()
        .totalCount(totalCount)
        .typeCounts(typeCounts)
        .nullCount(nullCount)
        .trueCount((trueCount == 0L) ? null : trueCount)
        .numberSummary(NumberSummary.fromNumberTracker(numberTracker))
        .frequentStringsSummary(
            cpcSketch.getEstimate() < 100
                ? FrequentStringsSummary.fromStringSketch(stringsSketch)
                : FrequentStringsSummary.empty())
        .build();
  }
}
