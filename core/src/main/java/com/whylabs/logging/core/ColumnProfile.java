package com.whylabs.logging.core;

import static com.whylabs.logging.core.SummaryConverters.fromSchemaTracker;
import static java.util.stream.Collectors.toSet;

import com.whylabs.logging.core.data.ColumnSummary;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.ColumnMessage;
import com.whylabs.logging.core.statistics.CountersTracker;
import com.whylabs.logging.core.statistics.NumberTracker;
import com.whylabs.logging.core.statistics.SchemaTracker;
import com.whylabs.logging.core.statistics.datatypes.StringTracker;
import com.whylabs.logging.core.types.TypedDataConverter;
import java.util.Set;
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

  public void track(Object value) {
    synchronized (this) {
      counters.incrementCount();

      if (value == null) {
        counters.incrementNull();
        return;
      }

      // always track text information
      // TODO: ignore this if we already know the data type
      if (value instanceof String) {
        stringTracker.update((String) value);
      }

      val typedData = TypedDataConverter.convert(value);
      schemaTracker.track(typedData.getType());

      switch (typedData.getType()) {
        case FRACTIONAL:
          numberTracker.track(typedData.getFractional());
          break;
        case INTEGRAL:
          numberTracker.track(typedData.getIntegralValue());
          break;
        case BOOLEAN:
          if (typedData.isBooleanValue()) {
            counters.incrementTrue();
          }
          break;
      }
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
