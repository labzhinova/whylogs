package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Serializer;
import java.util.Collections;
import java.util.List;

public abstract class ClassTaggedSerializer<T> extends Serializer<T> {

  public abstract Class<?> getClassTag();

  public List<Class<?>> getAdditionalClassTags() {
    return Collections.emptyList();
  }
}
