package ai.whylabs.profiler.core

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.text.NumberFormat
import java.util.Locale
import kotlin.system.measureTimeMillis

internal val NumberFormatter = NumberFormat.getNumberInstance(Locale.US)

fun main(args: Array<String>) {
    val initialFreeMemory = Runtime.getRuntime().freeMemory()
    val datasetProfile = DatasetProfile("testDataset")
    val executionTimeInMs = measureTimeMillis {
        // TODO: dynamic file path
        FileReader("/Users/andy/Downloads/US_Accidents_Dec19.csv").use {
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