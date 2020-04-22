package ai.whylabs.profile;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ProfilerDemo {

  public static void main(String[] args) throws Exception {
    val profile = new DatasetProfile("data", Instant.now());

    printAndFlush("Press enter", true);
    printAndFlush("Beginning", false);

    @Cleanup
    val fis =
        new FileInputStream(
            "/Users/andy/Downloads/Parking_Violations_Issued_-_Fiscal_Year_2017.csv");
    @Cleanup val reader = new InputStreamReader(fis);
    @Cleanup CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
    val spliterator = Spliterators.spliteratorUnknownSize(parser.iterator(), 0);
    Iterator<CSVRecord> it = StreamSupport.stream(spliterator, false).limit(1000_000).iterator();
    it.forEachRemaining(record -> profile.track(record.toMap()));
    printAndFlush("End. Press enter to run GC", true);
    printAndFlush("Running GC", false);
    Runtime.getRuntime().gc();
    printAndFlush("Ran GC. Press enter to exit", true);
    printAndFlush(DatasetProfile.Gson.toJson(profile.toInterpretableObject()), false);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @SneakyThrows
  private static void printAndFlush(String message, boolean shouldWait) {
    System.out.println(message);
    System.out.flush();

    if (shouldWait) {
      System.in.read();
    }
  }
}
