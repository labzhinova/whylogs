package ai.whylabs.profile;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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

@SuppressWarnings("Duplicates")
public class LendingClubDemo {
  private static final Scanner scanner = new Scanner(System.in);

  private static final Map<Instant, DatasetProfile> profiles = new HashMap<>();
  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyy-MM-dd");
//      DateTimeFormatter.ofPattern("MMM-yyyy").withLocale(Locale.ENGLISH);
  public static final String INPUT = "lendingclub_recjected_2007_to_2017.csv";

  public static void main(String[] args) throws Exception {
    printAndWait("Current process ID: " + ManagementFactory.getRuntimeMXBean().getName());

    String input = "/Users/andy/Downloads/reserach_data/lendingclub_rejected.json";

    @Cleanup
    val fis =
        new FileInputStream(
            "/Users/andy/Downloads/reserach_data/" + INPUT);
    @Cleanup val reader = new InputStreamReader(fis);
    CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString("");
    @Cleanup CSVParser parser = new CSVParser(reader, format);
    val spliterator = Spliterators.spliteratorUnknownSize(parser.iterator(), 0);
    StreamSupport.stream(spliterator, false)
        .iterator()
        .forEachRemaining(LendingClubDemo::normalTracking);
    try (val writer = new FileWriter(
        input)) {
      val interpretableDatasetProfileMap =
          profiles.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      e -> e.getKey().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE),
                      e -> e.getValue().toInterpretableObject()));
      DatasetProfile.Gson.toJson(interpretableDatasetProfileMap, writer);
    }
    printAndWait("Finished writing to file. Enter anything to exit");
  }

  /** Switch to #stressTest if we want to battle test the memory usage further */
  private static void normalTracking(CSVRecord record) {
    String issueDate = record.get("Application Date");
//    String issueDate = record.get("issue_d");
    val instant = toInstant(issueDate);
    profiles.compute(
        instant,
        (time, datasetProfile) -> {
          if (datasetProfile == null) {
            datasetProfile = new DatasetProfile(INPUT, time);
          }

          datasetProfile.track(record.toMap());
          return datasetProfile;
        });
  }

  private static Instant toInstant(String issueDate) {
    if (issueDate == null || issueDate.equalsIgnoreCase("nan")) {
      return Instant.ofEpochMilli(0);
//      return YearMonth.of(2000, 1).atDay(1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
    }

//    val ym = YearMonth.parse(issueDate, dateTimeFormatter);
//    return ym.atDay(1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
    return LocalDate.parse(issueDate, dateTimeFormatter)
        .atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
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

    //    String input = scanner.next();
    //    System.out.println("Got input: " + input);
    System.out.println();
    System.out.flush();
  }
}
