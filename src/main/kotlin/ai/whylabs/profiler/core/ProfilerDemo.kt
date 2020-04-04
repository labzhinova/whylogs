package ai.whylabs.profiler.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.text.NumberFormat
import java.util.Locale
import kotlin.system.measureTimeMillis

/**
 * Example test datasets:
 * * https://www.kaggle.com/sobhanmoosavi/us-accidents
 */
class ProfilerDemo : CliktCommand() {
    private val csvFile: String by option(help = "The CSV file path").prompt("CSV input")

    override fun run() {

        val initialFreeMemory = Runtime.getRuntime().freeMemory()
        val datasetProfile = DatasetProfile("DemoDataset")
        val executionTimeInMs = measureTimeMillis {
            FileReader(csvFile).use {
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
        internal val NumberFormatter = NumberFormat.getNumberInstance(Locale.US)
    }
}

fun main(args: Array<String>) = ProfilerDemo().main(args)