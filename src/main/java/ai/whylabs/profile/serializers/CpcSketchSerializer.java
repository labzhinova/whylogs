package ai.whylabs.profile.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.cpc.CpcSketch;

public class CpcSketchSerializer extends Serializer<CpcSketch> {

    @Override
    public void write(Kryo kryo, Output output, CpcSketch sketch) {
        kryo.writeObject(output, sketch.toByteArray());
    }

    @Override
    public CpcSketch read(Kryo kryo, Input input, Class<CpcSketch> type) {
        byte[] bytes = kryo.readObject(input, byte[].class);
        return CpcSketch.heapify(bytes);
    }
}
