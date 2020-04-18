package ai.whylabs.profiler.core

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.github.ajalt.clikt.core.CliktCommand
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.NumberFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.system.measureTimeMillis

/**
 * Example test datasets:
 * * https://www.kaggle.com/sobhanmoosavi/us-accidents
 */
class ProfilerDemo : CliktCommand() {
//    private val dataset: String by option(help = "Select S3 key").prompt("S3 key")

    override fun run() {
        val profiles = mutableMapOf<String, DatasetProfile>()
//        println("Loading data from: s3://$Databucket/$dataset")
//        val s3Object = S3.getObject(Databucket, dataset)
        val profile = DatasetProfile("data", Instant.now());
        val executionTimeInMs = measureTimeMillis {
            InputStreamReader(FileInputStream("/Users/andy/Downloads/Parking_Violations_Issued_-_Fiscal_Year_2017.csv")).use { reader ->
                val parser = CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
                for (record in parser) {
//                    val entryDate = LocalDate.parse(record.get("Issue Date"), DateTimeFormatter)
//                    val dateTime = entryDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
//                    val profile = profiles.getOrPut(
//                        entryDate.toString(),
//                        { DatasetProfile("Parking_Violations_Issued_-_Fiscal_Year_2017.csv", dateTime) })
                    profile.track(record.toMap())
                }
            }
        }

//        PrintWriter(FileOutputStream("/tmp/data.json")).use { writer ->
//            DatasetProfile.Gson.toJson(profiles.mapValues { e -> e.value.toInterpretableObject() }, writer)
//        }

        println(DatasetProfile.Gson.toJson(profile))

        println("Execution time (seconds): ${NumberFormatter.format(executionTimeInMs / 1000.0)}")
    }

    companion object {
        internal const val Databucket = "whylabs-test-data-public"
        internal val NumberFormatter = NumberFormat.getNumberInstance(Locale.US)
        internal val S3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build()
        val DateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .toFormatter()
    }
}

fun main(args: Array<String>) {
//    println("Listing file under bucket: ${ProfilerDemo.Databucket}")
//    for (o in S3Objects.inBucket(ProfilerDemo.S3, ProfilerDemo.Databucket)) {
//        println("Size : ${ProfilerDemo.NumberFormatter.format(o.size / 1_000_000.0)}(MBs). Path: ${o.key}")
//    }

    ProfilerDemo().main(args)
}