package com.whylabs.logging.core.statistics;

import com.whylabs.logging.core.data.InferredType;
import com.whylabs.logging.core.data.InferredType.Type;
import com.whylabs.logging.core.format.SchemaMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@EqualsAndHashCode
@RequiredArgsConstructor
public class SchemaTracker {
  @Getter
  @Builder(setterPrefix = "set", toBuilder = true, builderClassName = "Builder")
  public static class CalculatedType {
    InferredType.Type type;
    double ratio;

    public InferredType.Builder toProtobuf() {
      return InferredType.newBuilder().setType(type).setRatio(ratio);
    }

    static CalculatedType fromProtobuf(InferredType message) {
      return CalculatedType.builder()
          .setType(Optional.ofNullable(message.getType()).orElse(Type.UNKNOWN))
          .setRatio(message.getRatio())
          .build();
    }
  }

  private static final CalculatedType.Builder UNKNOWN_TYPE_BUILDER =
      CalculatedType.builder().setType(Type.UNKNOWN);

  private final Map<InferredType.Type, Long> typeCounts;

  public SchemaTracker() {
    this.typeCounts = new HashMap<>(Type.values().length, 1.0f);
  }

  @SuppressWarnings("unused")
  public void track(Object ignored) {
    updateTypeCount(InferredType.Type.UNKNOWN);
  }

  @SuppressWarnings("unused")
  public void track(long ignored) {
    updateTypeCount(Type.INTEGRAL);
  }

  @SuppressWarnings("unused")
  public void track(double ignored) {
    updateTypeCount(Type.FRACTIONAL);
  }

  @SuppressWarnings("unused")
  public void track(String ignored) {
    updateTypeCount(Type.STRING);
  }

  @SuppressWarnings("unused")
  public void track(boolean ignored) {
    updateTypeCount(Type.BOOLEAN);
  }

  private void updateTypeCount(Type type) {
    typeCounts.compute(type, (t, existing) -> Optional.ofNullable(existing).orElse(0L) + 1);
  }

  long getCount(InferredType.Type type) {
    return typeCounts.getOrDefault(type, 0L);
  }

  public Map<InferredType.Type, Long> getTypeCounts() {
    return Collections.unmodifiableMap(typeCounts);
  }

  public CalculatedType computeType() {
    val totalCount = typeCounts.values().stream().mapToLong(Long::longValue).sum();
    if (totalCount == 0) {
      return UNKNOWN_TYPE_BUILDER.build();
    }

    // first figure out the most popular type and its count
    val candidate = getMostPopularType(totalCount);
    if (candidate.ratio > 0.7) {
      return candidate;
    }

    // integral is a subset of fractional
    val fractionalCount =
        Stream.of(InferredType.Type.INTEGRAL, InferredType.Type.FRACTIONAL)
            .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
            .sum();

    // Handling String case first
    // it has to have more entries than fractional values
    if (candidate.type == Type.STRING && typeCounts.get(candidate.type) > fractionalCount) {
      // treat everything else as "String" except UNKNOWN
      val coercedCount =
          Stream.of(Type.STRING, Type.INTEGRAL, Type.FRACTIONAL, Type.BOOLEAN)
              .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
              .sum();
      val actualRatio = coercedCount / (double) totalCount;

      return CalculatedType.builder().setType(Type.STRING).setRatio(actualRatio).build();
    }

    // if not string but another type with majority
    if (candidate.ratio > 0.5) {
      long actualCount = typeCounts.get(candidate.type);
      if (candidate.type == Type.FRACTIONAL) {
        actualCount = fractionalCount;
      }

      return CalculatedType.builder()
          .setType(candidate.type)
          .setRatio(actualCount / (double) totalCount)
          .build();
    }

    // Otherwise, if fractional count is the majority, then likely this is a fractional type
    final double fractionalRatio = fractionalCount / (double) totalCount;
    if (fractionalRatio > 0.5) {
      return CalculatedType.builder().setType(Type.FRACTIONAL).setRatio(fractionalRatio).build();
    }

    return CalculatedType.builder().setType(candidate.type).setRatio(1.0).build();
  }

  public SchemaMessage.Builder toProtobuf() {
    val protobufFriendlyMap =
        typeCounts.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().getNumber(), Entry::getValue));

    return SchemaMessage.newBuilder().putAllTypeCounts(protobufFriendlyMap);
  }

  public static SchemaTracker fromProtobuf(SchemaMessage message) {
    val schemaTracker = new SchemaTracker();
    message
        .getTypeCountsMap()
        .forEach((k, v) -> schemaTracker.typeCounts.put(Type.forNumber(k), v));
    return schemaTracker;
  }

  private CalculatedType getMostPopularType(long totalCount) {
    val mostPopularType =
        typeCounts.entrySet().stream()
            .max((e1, e2) -> (int) (e1.getValue() - e2.getValue()))
            .map(Entry::getKey)
            .orElse(Type.UNKNOWN);

    val count = typeCounts.getOrDefault(mostPopularType, 0L);
    val ratio = count * 1.0 / totalCount;

    return CalculatedType.builder().setType(mostPopularType).setRatio(ratio).build();
  }
}
