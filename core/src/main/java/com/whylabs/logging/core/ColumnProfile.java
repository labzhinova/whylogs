package com.whylabs.logging.core;

import static com.whylabs.logging.core.SummaryConverters.fromSchemaTracker;
import static java.util.stream.Collectors.toSet;

import com.google.protobuf.Int64Value;
import com.whylabs.logging.core.data.ColumnSummary;
import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.statistics.Counters;
import com.whylabs.logging.core.statistics.NumberTracker;
import com.whylabs.logging.core.statistics.StringTracker;
import com.whylabs.logging.core.statistics.schema.SchemaTracker;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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

  private static final Set<Type> NUMERIC_TYPES =
      Stream.of(Type.FRACTIONAL, Type.INTEGRAL).collect(toSet());

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
      if (this.determinedType != null
          && this.determinedType.getType()
              == com.whylabs.logging.core.data.InferredType.Type.STRING) {
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

  public ColumnSummary toColumnSummary() {
    val counterBuilder =
        com.whylabs.logging.core.data.Counters.newBuilder().setCount(counters.getCount());
    if (counters.getNullCount() != null) {
      counterBuilder.setNullCount(Int64Value.of(counters.getNullCount()));
    }

    if (counters.getTrueCount() != null) {
      counterBuilder.setTrueCount(Int64Value.of(counters.getTrueCount()));
    }
    val schema = fromSchemaTracker(schemaTracker, determinedType);
    val builder = ColumnSummary.newBuilder().setCounters(counterBuilder.build()).setSchema(schema);

    if (schema.getInferredType().getType() == Type.STRING) {
      val stringSummary = SummaryConverters.fromStringTracker(stringTracker);
      if (stringSummary != null) {
        builder.setStringSummary(stringSummary);
      }
    } else if (NUMERIC_TYPES.contains(schema.getInferredType().getType())) {
      val numberSummary = SummaryConverters.fromNumberTracker(this.numberTracker);
      if (numberSummary != null) {
        builder.setNumberSummary(numberSummary);
      }
    }

    return builder.build();
  }
}
