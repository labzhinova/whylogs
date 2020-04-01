package ai.whylabs.profiler.core

fun main(args: Array<String>) {
    val datasetProfile = DatasetProfile("testDataset")
    for (i in 0..10_000_000) {
        datasetProfile.track("column${i % 2}", i)
    }

    println(datasetProfile.toJsonString())
}