package ai.whylabs.profile;

import ai.whylabs.profile.statistics.ColumnDataType;
import ai.whylabs.profile.statistics.Counters;
import ai.whylabs.profile.statistics.NumberTracker;
import ai.whylabs.profile.statistics.SchemaTracker;
import ai.whylabs.profile.statistics.StringTracker;
import ai.whylabs.profile.summary.NumberSummary;
import ai.whylabs.profile.summary.StringSummary;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ColumnProfile {

  private static final Pattern FRACTIONAL = Pattern.compile("^[-+]?( )?\\d+([.]\\d+)$");
  private static final Pattern INTEGRAL = Pattern.compile("^[-+]?( )?\\d+$");
  private static final Pattern BOOLEAN = Pattern.compile("^(?i)(true|false)$");

  final String columnName;
  final Map<ColumnDataType, Long> typeCounts;
  final Counters counters;
  final SchemaTracker schemaTracker;
  final NumberTracker numberTracker;
  final StringTracker stringTracker;

  public ColumnProfile(String columnName) {
    this(
        columnName,
        new EnumMap<>(ColumnDataType.class),
        new Counters(),
        new SchemaTracker(),
        new NumberTracker(),
        new StringTracker());
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

  public void track(Object value) {
    counters.incrementCount();

    if (value == null) {
      counters.incrementNull();
      return;
    }

    val normalizedData = normalizeType(value);
    if (normalizedData instanceof Number) {
      numberTracker.track((Number) normalizedData);
    } else if (normalizedData instanceof Boolean) {
      if ((Boolean) normalizedData) {
        counters.incrementTrue();
      }
    } else if (normalizedData instanceof String) {
      stringTracker.update((String) normalizedData);
    }

    schemaTracker.track(normalizedData);
  }

  public InterpretableColumnStatistics toInterpretableStatistics() {
    return InterpretableColumnStatistics.builder()
        .counters(counters)
        .typeCounts(typeCounts)
        .numberSummary(NumberSummary.fromNumberTracker(numberTracker))
        .stringSummary(StringSummary.fromTracker(stringTracker))
        .build();
  }
}
