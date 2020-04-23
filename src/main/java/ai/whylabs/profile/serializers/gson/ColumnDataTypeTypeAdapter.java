package ai.whylabs.profile.serializers.gson;

import ai.whylabs.profile.statistics.schema.ColumnDataType;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class ColumnDataTypeTypeAdapter extends TypeAdapter<ColumnDataType> {

  @Override
  public void write(JsonWriter out, ColumnDataType value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    out.value(value.getId());
  }

  @Override
  public ColumnDataType read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    return ColumnDataType.fromId(in.nextInt());
  }
}
