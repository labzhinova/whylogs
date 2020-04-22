package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import java.io.ByteArrayOutputStream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class KryoUtils {

  @SneakyThrows
  public <TYPE> TYPE doRoundTrip(Kryo kryo, TYPE original, Class<TYPE> clazz) {
    val bos = new ByteArrayOutputStream();
    val bbo = new ByteBufferOutput(bos);

    kryo.writeObjectOrNull(bbo, original, clazz);

    bbo.close();
    bos.close();

    val bbi = new ByteBufferInput(bos.toByteArray());
    return kryo.readObjectOrNull(bbi, clazz);
  }
}
