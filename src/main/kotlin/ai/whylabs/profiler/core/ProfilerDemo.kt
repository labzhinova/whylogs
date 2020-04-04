package ai.whylabs.profiler.core

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.iterable.S3Objects
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStreamReader
import java.text.NumberFormat
import java.util.Locale
import kotlin.system.measureTimeMillis

/**
 * Example test datasets:
 * * https://www.kaggle.com/sobhanmoosavi/us-accidents
 */
class ProfilerDemo : CliktCommand() {
    private val dataset: String by option(help = "Select S3 key").prompt("S3 key")

    override fun run() {
        println("Loading data from: s3://$Databucket/$dataset")
        val initialFreeMemory = Runtime.getRuntime().freeMemory()
        val datasetProfile = DatasetProfile(dataset)
        val s3Object = S3.getObject(Databucket, dataset)
        val executionTimeInMs = measureTimeMillis {
            InputStreamReader(s3Object.objectContent).use {
                val parser = CSVParser(it, CSVFormat.DEFAULT.withFirstRecordAsHeader())
                for (record in parser) {
                    datasetProfile.track(record.toMap())
                }
            }
            println(datasetProfile.toJsonString())
        }
        // force GC
        Runtime.getRuntime().gc()

        val usedMemory = initialFreeMemory - Runtime.getRuntime().freeMemory()
        println("Used memory (bytes): ${NumberFormatter.format(usedMemory)}")
        println("Execution time (seconds): ${NumberFormatter.format(executionTimeInMs / 1000.0)}")
    }

    companion object {
        internal const val Databucket = "whylabs-test-data-public"
        internal val NumberFormatter = NumberFormat.getNumberInstance(Locale.US)
        internal val S3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build()
    }
}

fun main(args: Array<String>) {
    println("Listing file under bucket: ${ProfilerDemo.Databucket}")
    for (o in S3Objects.inBucket(ProfilerDemo.S3, ProfilerDemo.Databucket)) {
        println("Size : ${ProfilerDemo.NumberFormatter.format(o.size / 1_000_000.0)}(MBs). Path: ${o.key}")
    }

    ProfilerDemo().main(args)
}