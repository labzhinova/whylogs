package ai.whylabs.profile;

import lombok.Cleanup;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class ProfilerDemo {
    public static void main(String[] args) throws Exception {
        val profile = new DatasetProfile("data", Instant.now());

        @Cleanup val fis = new FileInputStream("/Users/andy/Downloads/Parking_Violations_Issued_-_Fiscal_Year_2017.csv");
        @Cleanup val reader = new InputStreamReader(fis);
        @Cleanup CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        val spliterator = Spliterators.spliteratorUnknownSize(parser.iterator(), 0);
        Iterator<CSVRecord> it = StreamSupport.stream(spliterator, false).limit(1000_000).iterator();
        it.forEachRemaining(record -> profile.track(record.toMap()));
        System.out.println(DatasetProfile.Gson.toJson(profile.toInterpretableObject()));
    }
}
