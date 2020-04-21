package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Serializer;

public abstract class ClassTaggedSerializer<T> extends Serializer<T> {

  public abstract Class<?> getClassTag();
}
