package ai.whylabs.profile.statistics.schema;

import ai.whylabs.profile.serializers.kryo.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@EqualsAndHashCode
public class SchemaTracker implements KryoSerializable {

  private final transient ClassRegistrationHelper classHelper;

  @Expose @Getter private Map<ColumnDataType, Long> typeCounts;

  @Expose @EqualsAndHashCode.Exclude private InferredType determinedType;

  public SchemaTracker() {
    this.classHelper = new ClassRegistrationHelper(HashMap.class, ColumnDataType.class);
    this.typeCounts = new HashMap<>();
  }

  public void track(Object normalizedData) {
    val dataType = toEnumType(normalizedData);
    this.typeCounts.compute(
        dataType, (type, existingValue) -> existingValue == null ? 1L : existingValue + 1);
  }

  public ColumnDataType determineType() {
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
      return new InferredType(ColumnDataType.UNKNOWN, Double.NaN, 0);
    }

    // first figure out the most popular type and its count
    val candidate = getMostPopularType(totalCount);

    // integral is a subset of fractional
    val fractionalCount =
        Stream.of(ColumnDataType.INTEGRAL, ColumnDataType.FRACTIONAL)
            .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
            .sum();

    // Handling String case first
    // it has to have more entries than fractional values
    if (candidate.getType() == ColumnDataType.STRING && candidate.getCount() > fractionalCount) {
      // treat everything else as "String" except UNKNOWN
      val coercedCount =
          Stream.of(
                  ColumnDataType.STRING,
                  ColumnDataType.INTEGRAL,
                  ColumnDataType.FRACTIONAL,
                  ColumnDataType.BOOLEAN)
              .mapToLong(type -> typeCounts.getOrDefault(type, 0L))
              .sum();
      val actualRatio = coercedCount * 1.0 / totalCount;

      return new InferredType(ColumnDataType.STRING, actualRatio, coercedCount);
    }

    // if not string but another type with majority
    if (candidate.getRatio() > 0.5) {
      long actualCount = candidate.getCount();
      if (candidate.getType() == ColumnDataType.FRACTIONAL) {
        actualCount = fractionalCount;
      }
      return new InferredType(candidate.getType(), actualCount * 1.0 / totalCount, actualCount);
    }

    // Otherwise, if fractional count is the majority, then likely this is a fractional type
    if (fractionalCount > 0.5) {
      return new InferredType(
          candidate.getType(), fractionalCount * 1.0 / totalCount, fractionalCount);
    }

    return new InferredType(candidate.getType(), 1.0, totalCount);
  }

  private InferredType getMostPopularType(long totalCount) {
    val mostPopularType =
        typeCounts.entrySet().stream()
            .max((e1, e2) -> (int) (e1.getValue() - e2.getValue()))
            .map(Entry::getKey)
            .orElse(ColumnDataType.UNKNOWN);

    val count = typeCounts.getOrDefault(mostPopularType, 0L);
    val ratio = count * 1.0 / totalCount;
    return new InferredType(mostPopularType, ratio, count);
  }

  static ColumnDataType toEnumType(Object data) {
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

  @Override
  public void write(Kryo kryo, Output output) {
    this.classHelper.checkAndRegister(kryo);

    kryo.writeObject(output, typeCounts);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void read(Kryo kryo, Input input) {
    this.classHelper.checkAndRegister(kryo);

    this.typeCounts = (Map<ColumnDataType, Long>) kryo.readObject(input, HashMap.class);
  }
}
