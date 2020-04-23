package ai.whylabs.profile.serializers.kryo.helpers;

import com.esotericsoftware.kryo.Kryo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ClassRegistrationHelper {

  private final List<Class<?>> classes;

  @EqualsAndHashCode.Exclude private volatile boolean isRegistered;

  public ClassRegistrationHelper(Class<?> clazz) {
    this.classes = Collections.singletonList(clazz);
  }

  public ClassRegistrationHelper(Class<?>... classes) {
    this.classes = Arrays.asList(classes);
  }

  public void checkAndRegister(Kryo kryo) {
    if (!isRegistered) {
      classes.forEach(kryo::register);
      isRegistered = true;
    }
  }
}
