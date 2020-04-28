package com.whylabs.logging.core.serializers.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Base64;
import lombok.val;
import org.apache.datasketches.ArrayOfStringsSerDe;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.datasketches.memory.Memory;

public class ItemsSketchStringTypeAdapter extends TypeAdapter<ItemsSketch<String>> {

  @Override
  public void write(JsonWriter out, ItemsSketch<String> value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    val bytes = value.toByteArray(new ArrayOfStringsSerDe());
    out.value(Base64.getEncoder().encodeToString(bytes));
  }

  @Override
  public ItemsSketch<String> read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    val base64 = in.nextString();
    val bytes = Base64.getDecoder().decode(base64);

    return ItemsSketch.getInstance(Memory.wrap(bytes), new ArrayOfStringsSerDe());
  }
}
