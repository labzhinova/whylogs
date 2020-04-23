package ai.whylabs.profile.serializers.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Base64;

public class ByteArrayToBase64TypeAdapter extends TypeAdapter<byte[]> {

  @Override
  public void write(JsonWriter out, byte[] value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    out.value(Base64.getEncoder().encodeToString(value));
  }

  @Override
  public byte[] read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    String base64Text = in.nextString();
    return Base64.getDecoder().decode(base64Text);
  }
}
