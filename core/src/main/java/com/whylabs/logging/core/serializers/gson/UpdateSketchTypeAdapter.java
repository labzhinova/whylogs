package com.whylabs.logging.core.serializers.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Base64;
import lombok.val;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.theta.UpdateSketch;

public class UpdateSketchTypeAdapter extends TypeAdapter<UpdateSketch> {

  @Override
  public void write(JsonWriter out, UpdateSketch value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    val base64 = Base64.getEncoder().encodeToString(value.toByteArray());
    out.value(base64);
  }

  @Override
  public UpdateSketch read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    val bytes = Base64.getDecoder().decode(in.nextString());
    return UpdateSketch.heapify(Memory.wrap(bytes));
  }
}
