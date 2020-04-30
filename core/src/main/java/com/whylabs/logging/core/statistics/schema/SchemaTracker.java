package com.whylabs.logging.core.statistics.schema;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.SchemaMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class SchemaTracker {

  private static final InferredType UNKNOWN_TYPE =
      InferredType.newBuilder().setType(Type.UNKNOWN).setRatio(Double.NaN).setCount(0).build();

  private final Map<InferredType.Type, Long> typeCounts;
  private final InferredType.Builder inferredType;

  public SchemaTracker() {
    this.typeCounts = new HashMap<>();
    this.inferredType = UNKNOWN_TYPE.toBuilder();
  }

  public InferredType.Type getType() {
    return inferredType.getType();
  }

  public void track(Object normalizedData) {
    val dataType = toEnumType(normalizedData);
    if (dataType == inferredType.getType()) {
      inferredType.setCount(inferredType.getCount() + 1);
    }
    this.typeCounts.compute(
        dataType, (type, existingValue) -> existingValue == null ? 1L : existingValue + 1);
  }

  @NonNull
  public InferredType getOrComputeType() {
    if (this.inferredType.getType() != Type.UNKNOWN) {
      return this.inferredType.build();
    }

    this.computeType();
    return this.inferredType.build();
  }

  private void computeType() {
    val totalCount = typeCounts.values().stream().mapToLong(Long::longValue).sum();
    if (totalCount == 0) {
      return;
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

      inferredType.setType(Type.STRING).setCount(coercedCount).setRatio(actualRatio);
      return;
    }

    // if not string but another type with majority
    if (candidate.getRatio() > 0.5) {
      long actualCount = candidate.getCount();
      if (candidate.getType() == Type.FRACTIONAL) {
        actualCount = fractionalCount;
      }

      inferredType.mergeFrom(
          candidate
              .toBuilder()
              .setRatio(actualCount * 1.0 / totalCount)
              .setCount(actualCount)
              .build());
      return;
    }

    // Otherwise, if fractional count is the majority, then likely this is a fractional type
    if (fractionalCount > 0.5) {
      inferredType.mergeFrom(
          candidate
              .toBuilder()
              .setRatio(fractionalCount * 1.0 / totalCount)
              .setCount(fractionalCount)
              .build());
      return;
    }

    inferredType.mergeFrom(candidate.toBuilder().setRatio(1.0).setCount(totalCount).build());
  }

  public SchemaMessage.Builder toProtobuf() {
    val protoTypeCounts =
        typeCounts.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().getNumber(), Entry::getValue));
    return SchemaMessage.newBuilder()
        .putAllTypeCounts(protoTypeCounts)
        .setInferredType(inferredType);
  }

  public static SchemaTracker fromProtobuf(SchemaMessage message) {
    val typeCounts = new HashMap<Type, Long>();
    Optional.of(message.getTypeCountsMap())
        .ifPresent(m -> m.forEach((k, v) -> typeCounts.put(Type.forNumber(k), v)));

    return new SchemaTracker(typeCounts, message.getInferredType().toBuilder());
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
