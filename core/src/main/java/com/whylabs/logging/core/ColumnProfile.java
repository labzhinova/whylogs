package com.whylabs.logging.core;

import static com.whylabs.logging.core.SummaryConverters.fromSchemaTracker;
import static java.util.stream.Collectors.toSet;

import com.whylabs.logging.core.data.ColumnSummary;
import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.ColumnMessage;
import com.whylabs.logging.core.statistics.CountersTracker;
import com.whylabs.logging.core.statistics.NumberTracker;
import com.whylabs.logging.core.statistics.StringTracker;
import com.whylabs.logging.core.statistics.SchemaTracker;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(setterPrefix = "set")
public class ColumnProfile {

  private static final Pattern FRACTIONAL = Pattern.compile("^[-+]?( )?\\d+([.]\\d+)$");
  private static final Pattern INTEGRAL = Pattern.compile("^[-+]?( )?\\d+$");
  private static final Pattern BOOLEAN = Pattern.compile("^(?i)(true|false)$");

  private static final Set<Type> NUMERIC_TYPES =
      Stream.of(Type.FRACTIONAL, Type.INTEGRAL).collect(toSet());

  final String columnName;
  final CountersTracker counters;
  final SchemaTracker schemaTracker;
  final NumberTracker numberTracker;
  final StringTracker stringTracker;

  public ColumnProfile(String columnName) {
    this.columnName = columnName;
    this.counters = new CountersTracker();
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
      if (this.schemaTracker.getType() == InferredType.Type.STRING) {
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
      numberTracker.track((Number) normalizedData);
      stringTracker.update(value.toString());

    } else if (normalizedData instanceof Boolean) {
      if ((Boolean) normalizedData) {
        counters.incrementTrue();
      }
    } else if (normalizedData instanceof String) {
      stringTracker.update((String) normalizedData);
    }
  }

  public ColumnSummary toColumnSummary() {
    val schema = fromSchemaTracker(schemaTracker);

    val builder = ColumnSummary.newBuilder().setCounters(counters.toProtobuf());
    if (schema != null) {
      builder.setSchema(schema);
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
    }

    return builder.build();
  }

  public ColumnMessage.Builder toProtobuf() {
    return ColumnMessage.newBuilder()
        .setName(columnName)
        .setCounters(counters.toProtobuf())
        .setSchema(schemaTracker.toProtobuf())
        .setNumbers(numberTracker.toProtobuf())
        .setStrings(stringTracker.toProtobuf());
  }

  public static ColumnProfile fromProtobuf(ColumnMessage message) {
    return ColumnProfile.builder()
        .setColumnName(message.getName())
        .setCounters(CountersTracker.fromProtobuf(message.getCounters()))
        .setSchemaTracker(SchemaTracker.fromProtobuf(message.getSchema()))
        .setNumberTracker(NumberTracker.fromProtobuf(message.getNumbers()))
        .build();
  }
}
