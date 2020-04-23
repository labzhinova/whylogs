package ai.whylabs.profile;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Scanner;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ProfilerDemo {
  private static final Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) throws Exception {
    val profile = new DatasetProfile("data", Instant.now());

    printAndWait("Current process ID: " + ManagementFactory.getRuntimeMXBean().getName());

    @Cleanup
    val fis =
        new FileInputStream(
            "/Users/andy/Downloads/Parking_Violations_Issued_-_Fiscal_Year_2017.csv");
    @Cleanup val reader = new InputStreamReader(fis);
    @Cleanup CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
    val spliterator = Spliterators.spliteratorUnknownSize(parser.iterator(), 0);
    StreamSupport.stream(spliterator, false)
        .limit(1_000_000)
        .iterator()
        .forEachRemaining(record -> stressTest(profile, record));
    DatasetProfile.GsonCompact.toJson(profile, new FileWriter("/tmp/data.json"));
    printAndWait("Finished writing to file. Enter anything to exit");
  }

  /** Switch to #stressTest if we want to battle test the memory usage further */
  private static void normalTracking(DatasetProfile profile, CSVRecord record) {
    profile.track(record.toMap());
  }

  private static void stressTest(DatasetProfile profile, CSVRecord record) {
    for (int i = 0; i < 10; i++) {
      int finalI = i;
      val modifiedMap =
          record.toMap().entrySet().stream()
              .collect(Collectors.toMap(e -> e.getKey() + finalI, e -> e.getValue() + finalI));

      profile.track(modifiedMap);
    }
  }

  @SneakyThrows
  private static void printAndWait(String message) {
    System.out.print(message + ": ");

    String input = scanner.next();
    System.out.println("Got input: " + input);
    System.out.flush();
  }
}
