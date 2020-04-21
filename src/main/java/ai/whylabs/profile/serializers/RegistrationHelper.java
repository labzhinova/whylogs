package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;

public class RegistrationHelper {

  private volatile boolean isRegistered;

  public void checkByteArray(Kryo kryo) {
    if (!isRegistered) {
      kryo.register(byte[].class);
      isRegistered = true;
    }
  }
}
