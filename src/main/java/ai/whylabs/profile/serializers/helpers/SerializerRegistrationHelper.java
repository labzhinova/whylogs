package ai.whylabs.profile.serializers.helpers;

import ai.whylabs.profile.serializers.ClassTaggedSerializer;
import com.esotericsoftware.kryo.Kryo;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SerializerRegistrationHelper {

  private final List<ClassTaggedSerializer<?>> serializers;

  @EqualsAndHashCode.Exclude
  private volatile boolean isRegistered;

  public SerializerRegistrationHelper(ClassTaggedSerializer<?>... serializers) {
    this.serializers = Arrays.asList(serializers);
  }

  public void checkAndRegister(Kryo kryo) {
    if (!isRegistered) {
      for (ClassTaggedSerializer<?> serializer : serializers) {
        kryo.register(serializer.getClassTag(), serializer);

        for (Class<?> additionalClazz : serializer.getAdditionalClassTags()) {
          kryo.register(additionalClazz, serializer);
        }
      }

      isRegistered = true;
    }
  }
}
