package ai.whylabs.profile.statistics;

import ai.whylabs.profile.ColumnDataType;
import ai.whylabs.profile.serializers.helpers.ClassRegistrationHelper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

@EqualsAndHashCode
public class SchemaTracker implements KryoSerializable {

  private final ClassRegistrationHelper classHelper;

  @Getter
  private Map<ColumnDataType, Long> typeCounts;

  public SchemaTracker() {
    this.classHelper = new ClassRegistrationHelper(HashMap.class, ColumnDataType.class);
    this.typeCounts = new HashMap<>();
  }

  public void track(Object normalizedData) {
    val dataType = toEnumType(normalizedData);
    this.typeCounts.compute(
        dataType,
        (type, existingValue) -> existingValue == null ? 1L : existingValue + 1);

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
