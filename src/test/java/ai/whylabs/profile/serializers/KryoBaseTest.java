package ai.whylabs.profile.serializers;

import static org.junit.Assert.assertNull;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.val;

public abstract class KryoBaseTest<TYPE> {

  private final Class<TYPE> clazz;
  private final Kryo kryo;

  public KryoBaseTest(Serializer<TYPE> serializer, Class<TYPE> clazz) {
    this.kryo = new Kryo();
    this.kryo.register(clazz, serializer);
    this.clazz = clazz;
  }

  public abstract TYPE createObject();

  public abstract void verify(TYPE original, TYPE deserializedObject);

  @SneakyThrows
  public void runFullSerialization() {
    val original = createObject();
    TYPE deserializedObject = doRoundTrip(original);

    verify(original, deserializedObject);
  }

  @SneakyThrows
  public void runWithNull() {
    TYPE deserializedObject = doRoundTrip(null);

    assertNull(deserializedObject);
  }

  private TYPE doRoundTrip(TYPE original) throws IOException {
    val bos = new ByteArrayOutputStream();
    val bbo = new ByteBufferOutput(bos);
    kryo.writeObjectOrNull(bbo, original, clazz);
    bbo.close();
    bos.close();

    val bbi = new ByteBufferInput(bos.toByteArray());
    return kryo.readObjectOrNull(bbi, clazz);
  }
}
