package ai.whylabs.profiler.jvm;

import lombok.Data;

@Data
public class ColumProfile {


    public enum ColumnDataType {
        NULL,
        FRACTIONAL,
        INTEGRAL,
        BOOLEAN,
        STRING,
        UNKNOWN,
    }

}
