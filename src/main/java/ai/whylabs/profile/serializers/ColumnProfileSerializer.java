package ai.whylabs.profile.serializers;

import ai.whylabs.profile.ColumnProfile;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.datasketches.cpc.CpcSketch;

public class ColumnProfileSerializer extends Serializer<ColumnProfile> {
    @Override
    public void write(Kryo kryo, Output output, ColumnProfile columnProfile) {
        kryo.register(CpcSketch.class, new CpcSketchSerializer());

        kryo.writeObject(output, columnProfile.getColumnName());

        // Counting
        kryo.writeObject(output, columnProfile.getNullCount());
        kryo.writeObject(output, columnProfile.getTrueCount());
        kryo.writeObject(output, columnProfile.getTypeCounts());

        // Numerical summaries
        kryo.writeObject(output, columnProfile.getDoubleSummary());
        kryo.writeObject(output, columnProfile.getLongSummary());
        kryo.writeObject(output, columnProfile.getStddevSummary());

        // Sketches
        kryo.writeObject(output, columnProfile.getCpcSketch());
        kryo.writeObject(output, columnProfile.getStringsSketch());
        kryo.writeObject(output, columnProfile.getNumbersSketch());
    }

    @Override
    public ColumnProfile read(Kryo kryo, Input input, Class<ColumnProfile> type) {
        return null;
    }
}
