package ai.whylabs.profile.serializers.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Base64;
import lombok.val;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

public class UpdateDoublesSketchTypeAdapter extends TypeAdapter<UpdateDoublesSketch> {
  @Override
  public void write(JsonWriter out, UpdateDoublesSketch value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    val bytes = value.toByteArray();
    out.value(Base64.getEncoder().encodeToString(bytes));
  }

  @Override
  public UpdateDoublesSketch read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    String base64Text = in.nextString();
    val bytes = Base64.getDecoder().decode(base64Text);

    return UpdateDoublesSketch.heapify(Memory.wrap(bytes));
  }
}
