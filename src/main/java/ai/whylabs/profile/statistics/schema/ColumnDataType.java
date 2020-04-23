package ai.whylabs.profile.statistics.schema;

import lombok.Getter;

public enum ColumnDataType {
  NULL(0),
  FRACTIONAL(1),
  INTEGRAL(2),
  BOOLEAN(3),
  STRING(4),
  UNKNOWN(999),
  ;

  // use an internal ID for efficient serialization
  // cannot rely on Java's internal ID since it's not consistent across JVM
  // also, devs might accidentally change the order and cause future bugs
  @Getter
  private final int id;

  ColumnDataType(int id) {
    this.id = id;
  }

  public static ColumnDataType fromId(int id) {
    for (ColumnDataType value : values()) {
      if (value.id == id) {
        return value;
      }
    }

    return UNKNOWN;
  }
}
