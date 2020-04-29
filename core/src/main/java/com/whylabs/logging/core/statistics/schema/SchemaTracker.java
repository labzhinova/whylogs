package com.whylabs.logging.core.statistics.schema;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@EqualsAndHashCode
public class SchemaTracker {

  @Getter private final Map<InferredType.Type, Long> typeCounts;

  @EqualsAndHashCode.Exclude private InferredType determinedType;

  public SchemaTracker() {
    this.typeCounts = new HashMap<>();
  }

  public void track(Object normalizedData) {
    val dataType = toEnumType(normalizedData);
    this.typeCounts.compute(
        dataType, (type, existingValue) -> existingValue == null ? 1L : existingValue + 1);
  }

  public InferredType.Type determineType() {
    if (determinedType != null) {
      return determinedType.getType();
    }

    val inferredType = this.getDeterminedType();
    if (inferredType.getRatio() < 0.8) {
      return null;
    }

    this.determinedType = inferredType;
    return inferredType.getType();
  }

  public InferredType getDeterminedType() {
    val totalCount = typeCounts.values().stream().mapToLong(Long::longValue).sum();
    if (totalCount == 0) {
      return InferredType.newBuilder()
          .setType(Type.UNKNOWN)
          .setRatio(Double.NaN)
          .setCount(0)
          .build();
    }

    // first figure out the most popular type and its count
    val candidate = getMostPopularType(totalCount);

    // integral is a subset of fractional
    val fractionalCount =
        Stream.of(InferredType.Type.INTEGRAL, InferredType.Type.FRACTIONAL)
            .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
            .sum();

    // Handling String case first
    // it has to have more entries than fractional values
    if (candidate.getType() == Type.STRING && candidate.getCount() > fractionalCount) {
      // treat everything else as "String" except UNKNOWN
      val coercedCount =
          Stream.of(Type.STRING, Type.INTEGRAL, Type.FRACTIONAL, Type.BOOLEAN)
              .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
              .sum();
      val actualRatio = coercedCount * 1.0 / totalCount;

      return InferredType.newBuilder()
          .setType(Type.STRING)
          .setCount(coercedCount)
          .setRatio(actualRatio)
          .build();
    }

    // if not string but another type with majority
    if (candidate.getRatio() > 0.5) {
      long actualCount = candidate.getCount();
      if (candidate.getType() == Type.FRACTIONAL) {
        actualCount = fractionalCount;
      }

      return candidate
          .toBuilder()
          .setRatio(actualCount * 1.0 / totalCount)
          .setCount(actualCount)
          .build();
    }

    // Otherwise, if fractional count is the majority, then likely this is a fractional type
    if (fractionalCount > 0.5) {
      return candidate
          .toBuilder()
          .setRatio(fractionalCount * 1.0 / totalCount)
          .setCount(fractionalCount)
          .build();
    }

    return candidate.toBuilder().setRatio(1.0).setCount(totalCount).build();
  }

  private InferredType getMostPopularType(long totalCount) {
    val mostPopularType =
        typeCounts.entrySet().stream()
            .max((e1, e2) -> (int) (e1.getValue() - e2.getValue()))
            .map(Entry::getKey)
            .orElse(InferredType.Type.UNKNOWN);

    val count = typeCounts.getOrDefault(mostPopularType, 0L);
    val ratio = count * 1.0 / totalCount;

    return InferredType.newBuilder()
        .setType(mostPopularType)
        .setRatio(ratio)
        .setCount(count)
        .build();
  }

  static InferredType.Type toEnumType(Object data) {
    if (data instanceof String) {
      return InferredType.Type.STRING;
    }

    if (data instanceof Long) {
      return InferredType.Type.INTEGRAL;
    }

    if (data instanceof Double) {
      return InferredType.Type.FRACTIONAL;
    }

    if (data instanceof Boolean) {
      return InferredType.Type.BOOLEAN;
    }

    return InferredType.Type.UNKNOWN;
  }
}
