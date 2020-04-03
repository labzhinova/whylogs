package ai.whylabs.profiler.core

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader

fun main(args: Array<String>) {
    val datasetProfile = DatasetProfile("testDataset")

    // TODO: dynamic file path
    FileReader("/Users/andy/Downloads/202001-rma-csv-collection.csv").use {
        val parser = CSVParser(it, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        for (record in parser) {
            for (col in parser.headerNames) {
                datasetProfile.track(col, record.get(col))
            }
        }
    }
    println(datasetProfile.toJsonString())
}