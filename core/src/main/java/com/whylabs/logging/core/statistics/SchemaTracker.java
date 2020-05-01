package com.whylabs.logging.core.statistics;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Builder;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.SchemaMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class SchemaTracker {

  private static final InferredType UNKNOWN_TYPE =
      InferredType.newBuilder().setType(Type.UNKNOWN).build();

  private final SchemaMessage.Builder proto;

  public SchemaTracker() {
    this.proto = SchemaMessage.newBuilder().setInferredType(UNKNOWN_TYPE.toBuilder());
  }

  public InferredType.Type getType() {
    return proto.getInferredType().getType();
  }

  public void track(Object normalizedData) {
    val dataType = toEnumType(normalizedData);
    if (dataType == this.getType()) {
      val inferredTypeBuilder = getInferredTypeBuilder();
      inferredTypeBuilder.setCount(inferredTypeBuilder.getCount() + 1);
    }
    val existing = getTypeCounts().getOrDefault(dataType.getNumber(), 0L);
    this.proto.putTypeCounts(dataType.getNumber(), existing + 1);
  }

  Map<Integer, Long> getTypeCounts() {
    return this.proto.getTypeCountsMap();
  }

  private Builder getInferredTypeBuilder() {
    return proto.getInferredTypeBuilder();
  }

  @NonNull
  public InferredType getOrComputeType() {
    if (getType() != Type.UNKNOWN) {
      return getInferredTypeBuilder().build();
    }

    this.computeType();
    return getInferredTypeBuilder().build();
  }

  private void computeType() {
    val typeCounts = getTypeCounts();
    val totalCount = typeCounts.values().stream().mapToLong(Long::longValue).sum();
    if (totalCount == 0) {
      return;
    }

    // first figure out the most popular type and its count
    val candidate = getMostPopularType(totalCount);

    // integral is a subset of fractional
    val fractionalCount =
        Stream.of(InferredType.Type.INTEGRAL, InferredType.Type.FRACTIONAL)
            .mapToLong(type -> typeCounts.getOrDefault(type.getNumber(), 0L))
            .sum();

    // Handling String case first
    // it has to have more entries than fractional values
    if (candidate.getType() == Type.STRING && candidate.getCount() > fractionalCount) {
      // treat everything else as "String" except UNKNOWN
      val coercedCount =
          Stream.of(Type.STRING, Type.INTEGRAL, Type.FRACTIONAL, Type.BOOLEAN)
              .mapToLong(type -> typeCounts.getOrDefault(type.getNumber(), 0L))
              .sum();
      val actualRatio = coercedCount * 1.0 / totalCount;

      getInferredTypeBuilder().setType(Type.STRING).setCount(coercedCount).setRatio(actualRatio);
      return;
    }

    // if not string but another type with majority
    if (candidate.getRatio() > 0.5) {
      long actualCount = candidate.getCount();
      if (candidate.getType() == Type.FRACTIONAL) {
        actualCount = fractionalCount;
      }

      getInferredTypeBuilder()
          .mergeFrom(
              candidate
                  .toBuilder()
                  .setRatio(actualCount * 1.0 / totalCount)
                  .setCount(actualCount)
                  .build());
      return;
    }

    // Otherwise, if fractional count is the majority, then likely this is a fractional type
    if (fractionalCount > 0.5) {
      getInferredTypeBuilder()
          .mergeFrom(
              candidate
                  .toBuilder()
                  .setRatio(fractionalCount * 1.0 / totalCount)
                  .setCount(fractionalCount)
                  .build());
      return;
    }

    getInferredTypeBuilder()
        .mergeFrom(candidate.toBuilder().setRatio(1.0).setCount(totalCount).build());
  }

  public SchemaMessage toProtobuf() {
    return proto.build();
  }

  public static SchemaTracker fromProtobuf(SchemaMessage message) {
    val typeCounts = new HashMap<Type, Long>();
    Optional.of(message.getTypeCountsMap())
        .ifPresent(m -> m.forEach((k, v) -> typeCounts.put(Type.forNumber(k), v)));

    return new SchemaTracker(message.toBuilder());
  }

  private InferredType getMostPopularType(long totalCount) {
    val typeCountsMap = getTypeCounts();
    val mostPopularTypeNumber =
        typeCountsMap.entrySet().stream()
            .max((e1, e2) -> (int) (e1.getValue() - e2.getValue()))
            .map(Entry::getKey)
            .orElse(InferredType.Type.UNKNOWN.getNumber());

    val count = typeCountsMap.getOrDefault(mostPopularTypeNumber, 0L);
    val ratio = count * 1.0 / totalCount;

    return InferredType.newBuilder()
        .setType(Type.forNumber(mostPopularTypeNumber))
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
