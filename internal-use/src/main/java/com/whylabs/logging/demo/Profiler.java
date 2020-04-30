package com.whylabs.logging.demo;

import com.google.protobuf.util.JsonFormat;
import com.whylabs.logging.core.DatasetProfile;
import com.whylabs.logging.core.data.DatasetSummaries;
import com.whylabs.logging.core.datetime.EasyDateTimeParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "profiler",
    description = "Run WhyLogs profiling against custom CSV dataset",
    mixinStandardHelpOptions = true)
public class Profiler implements Runnable {

  private static final Scanner SCANNER = new Scanner(System.in);
  private static final CSVFormat CSV_FORMAT =
      CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString("");

  @Option(
      names = {"-i", "--input"},
      paramLabel = "CSV_INPUT_FILE",
      description = "input csv path",
      required = true)
  File input;

  @Option(
      names = {"-o", "--output"},
      paramLabel = "JSON_OUTPUT_FILE",
      description =
          "output json file. By default the program will write to a file the same input folder using the CSV file name as a base")
  File output;

  @Option(
      names = {"-l", "--limit"},
      paramLabel = "LIMIT_NUMBER",
      description =
          "limit the number of entries to process. Can be used to quickly validate the command (default: ${DEFAULT-VALUE})")
  int limit = -1;

  @Option(
      names = {"-s", "--separator"},
      paramLabel = "SEPARATOR_CHARACTOR",
      description = "record separator. For tab character please use '\\t'")
  String delimiter = ",";

  @ArgGroup(exclusive = false)
  Dependent datetime;

  private EasyDateTimeParser dateTimeParser;

  static class Dependent {
    @Option(
        names = {"-d", "--datetime"},
        description =
            "the column for parsing the datetime. If missing, we assume the dataset is running in batch mode",
        required = true)
    String column;

    @Option(
        names = {"-f", "--format"},
        description =
            "Format of the datetime column. Must specified if the datetime column is specified. "
                + "For epoch second please use 'epoch', and 'epochMillis' for epoch milliseconds",
        required = true)
    String format;
  }

  private final Map<Instant, DatasetProfile> profiles = new HashMap<>();

  @SneakyThrows
  @Override
  public void run() {
    validateFiles();

    @SuppressWarnings("deprecation")
    val unescapedDelimiter = StringEscapeUtils.unescapeJava(delimiter);
    if (unescapedDelimiter.length() != 1) {
      printErrorAndExit("Separator must be 1 character only (excluding escape characters)");
    }

    if (datetime != null) {
      System.out.printf("Using date time format: %s\n", datetime.format);
      System.out.printf("Using date time column: %s\n", datetime.column);
      this.dateTimeParser = new EasyDateTimeParser(datetime.format);
    }

    try {
      System.out.printf("Reading input from: %s\n", input);
      @Cleanup val fis = new FileInputStream(input);
      @Cleanup val reader = new InputStreamReader(fis);
      val csvFormat = CSV_FORMAT.withDelimiter(unescapedDelimiter.charAt(0));
      @Cleanup CSVParser parser = new CSVParser(reader, csvFormat);
      if (datetime != null) {
        if (!parser.getHeaderMap().containsKey(datetime.column)) {
          printErrorAndExit(
              "Column does not exist in the CSV header: %s. Headers: %s",
              datetime.column, parser.getHeaderMap());
        }
      }
      val spliterator = Spliterators.spliteratorUnknownSize(parser.iterator(), 0);
      val records =
          (limit > 0)
              ? StreamSupport.stream(spliterator, false).limit(limit)
              : StreamSupport.stream(spliterator, false);

      if (limit > 0) {
        System.out.printf("Limit stream to length: %d\n", limit);
      }

      records.forEach(this::normalTracking);

      System.out.println(
          "Finished collecting statistics. Writing to output file: " + output.getAbsolutePath());

      val profilesBuilder = DatasetSummaries.newBuilder();
      profiles.forEach(
          (k, profile) -> {
            final String timestamp =
                k.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
            profilesBuilder.putProfiles(timestamp, profile.toSummary());
          });

      try (val writer = new FileWriter(output)) {
        JsonFormat.printer().appendTo(profilesBuilder, writer);
      }
      printAndWait("Finished writing to file. Enter anything to exit");
      System.out.println("SUCCESS");
    } catch (Exception e) {
      if (!output.delete()) {
        System.err.println("Failed to clean up output file: " + output.getAbsolutePath());
        e.printStackTrace();
      }
    }
  }

  @SneakyThrows
  private void validateFiles() {
    if (!input.exists()) {
      printErrorAndExit("ABORTING! Input file does not exist at: %s", input.getAbsolutePath());
    }
    val inputFileName = input.getName();
    val extension = FilenameUtils.getExtension(inputFileName);
    if (!"csv".equalsIgnoreCase(extension) && !"tsv".equalsIgnoreCase(extension)) {
      System.err.printf("WARNING: Input does not have CSV extension. Got: %s\n", extension);
    }

    if (output == null) {
      val parentFolder = input.toPath().toAbsolutePath().getParent();
      val baseName = FilenameUtils.removeExtension(inputFileName);
      val now = ZonedDateTime.now();
      val today = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
      val secondOfDay = now.get(ChronoField.SECOND_OF_DAY);
      val outputFileName =
          MessageFormat.format("{0}.{1}-{2,number,#}.json", baseName, today, secondOfDay);
      output = parentFolder.resolve(outputFileName).toFile();
    }

    if (output.exists()) {
      printErrorAndExit("ABORTING! Output file already exists at: %s", output.getAbsolutePath());
    }

    if (!output.createNewFile()) {
      printErrorAndExit(
          "ABORTING! Failed to create new output file at: %s", output.getAbsolutePath());
    }
  }

  private void printErrorAndExit(String message, Object... args) {
    System.out.printf(message, args);
    System.out.println();
    System.exit(1);
  }

  /** Switch to #stressTest if we want to battle test the memory usage further */
  private void normalTracking(CSVRecord record) {
    String issueDate = record.get(this.datetime.column);
    val time = this.dateTimeParser.parse(issueDate);
    profiles.compute(
        time,
        (t, ds) -> {
          if (ds == null) {
            ds = new DatasetProfile(input.getName(), t);
          }

          ds.track(record.toMap());
          return ds;
        });
  }

  private void stressTest(DatasetProfile profile, CSVRecord record) {
    for (int i = 0; i < 10; i++) {
      int finalI = i;
      val modifiedMap =
          record.toMap().entrySet().stream()
              .collect(Collectors.toMap(e -> e.getKey() + finalI, e -> e.getValue() + finalI));

      profile.track(modifiedMap);
    }
  }

  private static void printAndWait(String message) {
    System.out.println(message);
    System.out.flush();
    SCANNER.nextLine();
  }

  public static void main(String[] args) {
    new CommandLine(new Profiler()).execute(args);
  }
}
