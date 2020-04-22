package ai.whylabs.profile.serializers;

import static org.junit.Assert.assertNull;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import lombok.SneakyThrows;
import lombok.val;

public abstract class KryoBaseTest<TYPE> {

  private final Class<TYPE> clazz;
  private final Kryo kryo;

  public KryoBaseTest(Serializer<? extends TYPE> serializer, Class<TYPE> clazz) {
    this.kryo = new Kryo();
    this.kryo.register(clazz, serializer);
    this.kryo.register(byte[].class);

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

  private TYPE doRoundTrip(TYPE original) {
    return KryoUtils.doRoundTrip(kryo, original, clazz);
  }
}
