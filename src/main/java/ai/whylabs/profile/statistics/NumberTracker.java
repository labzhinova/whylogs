package ai.whylabs.profile.statistics;

import ai.whylabs.profile.serializers.CpcSketchSerializer;
import ai.whylabs.profile.serializers.HeapUpdateDoublesSketchSerializer;
import ai.whylabs.profile.serializers.helpers.ClassRegistrationHelper;
import ai.whylabs.profile.serializers.helpers.SerializerRegistrationHelper;
import ai.whylabs.profile.statistics.trackers.DoubleTracker;
import ai.whylabs.profile.statistics.trackers.LongTracker;
import ai.whylabs.profile.statistics.trackers.VarianceTracker;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;
import org.apache.datasketches.cpc.CpcSketch;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;

@Getter
public class NumberTracker implements KryoSerializable {

  private final ClassRegistrationHelper classHelper;
  private final SerializerRegistrationHelper serializerHelper;

  // our own trackers
  private VarianceTracker stddev;
  private DoubleTracker doubles;
  private LongTracker longs;

  // sketches
  private CpcSketch cpcSketch; // unique counting
  private UpdateDoublesSketch numbersSketch; // histogram

  public NumberTracker() {
    this.classHelper =
        new ClassRegistrationHelper(VarianceTracker.class, DoubleTracker.class, LongTracker.class);
    this.serializerHelper =
        new SerializerRegistrationHelper(
            new CpcSketchSerializer(), new HeapUpdateDoublesSketchSerializer());

    this.stddev = new VarianceTracker();
    this.doubles = new DoubleTracker();
    this.longs = new LongTracker();

    this.cpcSketch = new CpcSketch();
    this.numbersSketch = DoublesSketch.builder().setK(256).build();
  }

  public void track(Number number) {
    double dValue = number.doubleValue();
    stddev.update(dValue);
    cpcSketch.update(dValue);
    numbersSketch.update(dValue);

    if (doubles.getCount() > 0) {
      doubles.update(dValue);
    } else if (number instanceof Long || number instanceof Integer) {
      longs.update(number.longValue());
    } else {
      doubles.addLongs(longs);
      longs.reset();
      doubles.update(dValue);
    }
  }

  @Override
  public void write(Kryo kryo, Output output) {
    this.classHelper.checkAndRegister(kryo);
    this.serializerHelper.checkAndRegister(kryo);

    kryo.writeObject(output, stddev);
    kryo.writeObject(output, doubles);
    kryo.writeObject(output, longs);
    kryo.writeObject(output, cpcSketch);
    kryo.writeObject(output, numbersSketch);
  }

  @Override
  public void read(Kryo kryo, Input input) {
    this.classHelper.checkAndRegister(kryo);
    this.serializerHelper.checkAndRegister(kryo);

    this.stddev = kryo.readObject(input, VarianceTracker.class);
    this.doubles = kryo.readObject(input, DoubleTracker.class);
    this.longs = kryo.readObject(input, LongTracker.class);
    this.cpcSketch = kryo.readObject(input, CpcSketch.class);
    this.numbersSketch = kryo.readObject(input, UpdateDoublesSketch.class);
  }
}
