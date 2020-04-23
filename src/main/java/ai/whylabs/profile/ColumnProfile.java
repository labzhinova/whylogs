package ai.whylabs.profile;

import ai.whylabs.profile.statistics.Counters;
import ai.whylabs.profile.statistics.NumberTracker;
import ai.whylabs.profile.statistics.StringTracker;
import ai.whylabs.profile.statistics.schema.ColumnDataType;
import ai.whylabs.profile.statistics.schema.InferredType;
import ai.whylabs.profile.statistics.schema.SchemaTracker;
import ai.whylabs.profile.summary.NumberSummary;
import ai.whylabs.profile.summary.SchemaSummary;
import ai.whylabs.profile.summary.StringSummary;
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
  final Counters counters;
  final SchemaTracker schemaTracker;
  NumberTracker numberTracker;
  StringTracker stringTracker;
  InferredType determinedType;

  public ColumnProfile(String columnName) {
    this.columnName = columnName;
    this.counters = new Counters();
    this.schemaTracker = new SchemaTracker();
    this.numberTracker = new NumberTracker();
    this.stringTracker = new StringTracker();
  }

  private Object normalizeType(Object data) {
    if (data == null) {
      return null;
    }

    if (data instanceof String) {
      val strData = (String) data;

      // ignore pattern matching if we already determine the type
      if (this.determinedType != null && this.determinedType.getType() == ColumnDataType.STRING) {
        return data;
      }

      if (INTEGRAL.matcher(strData).matches()) {
        return Long.parseLong(strData);
      }
      if (FRACTIONAL.matcher(strData).matches()) {
        return Double.parseDouble(strData);
      }
      if (BOOLEAN.matcher(strData).matches()) {
        return Boolean.parseBoolean(strData);
      }

      return data;
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
    schemaTracker.track(normalizedData);

    if (normalizedData instanceof Number) {
      if (numberTracker != null) {
        numberTracker.track((Number) normalizedData);
      }
      if (stringTracker != null) {
        // in case of numbers, we also track them as string here
        stringTracker.update((String) value);
      }
    } else if (normalizedData instanceof Boolean) {
      if ((Boolean) normalizedData) {
        counters.incrementTrue();
      }
    } else if (normalizedData instanceof String) {
      if (stringTracker != null) {
        stringTracker.update((String) normalizedData);
      }
    }
  }

  public void compact() {
    val determinedType = schemaTracker.determineType();
    if (determinedType == null) {
      // we can't do anything yet. Just back off
      return;
    }

    switch (determinedType) {
      case STRING:
        this.numberTracker = null;
        break;
      case FRACTIONAL:
      case INTEGRAL:
        this.stringTracker = null;
        break;
      case BOOLEAN:
        this.numberTracker = null;
        this.stringTracker = null;
        break;
      default:
        // do nothing
    }
  }

  public InterpretableColumnStatistics toInterpretableStatistics() {
    return InterpretableColumnStatistics.builder()
        .counters(counters)
        .schema(SchemaSummary.fromTracker(schemaTracker, determinedType))
        .numberSummary(NumberSummary.fromNumberTracker(numberTracker))
        .stringSummary(StringSummary.fromTracker(stringTracker))
        .build();
  }
}
