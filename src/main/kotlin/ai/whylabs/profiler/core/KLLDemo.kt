package ai.whylabs.profiler.core

import com.google.gson.*
import org.apache.datasketches.kll.KllFloatsSketch
import org.apache.datasketches.theta.UpdateSketch
import java.lang.reflect.Type
import java.util.*



val FractionalPattern = Regex("^[-+]?( )?\\d+([,.]\\d+)$")
val IntegralPattern = Regex("^[-+]?( )?\\d+$")
val Boolean = Regex("^(?i)(true|false)$")



fun main(args: Array<String>) {
    val sketch = KllFloatsSketch()
    val sketch1 = UpdateSketch.builder().build()

    val columnProfile = ColumnProfile("test")
    val datasetProfile = DatasetProfile("testDataset")
    for (i in 0..10_000_000) {
//        columnProfile.track(i)
        columnProfile.track("foobar$i")
    }

    System.out.println(columnProfile.toJsonString())
//    FileOutputStream("/tmp/test.bin").use {
//        it.write(sketch.toByteArray())
//    }
//
//    sketch.toByteArray()
//
//    val median = sketch.getQuantile(0.5)
//    println("Median is: $median")
//    val rankOf1k = sketch.getRank(1000.toFloat())
//    println("Rank of 1k: $rankOf1k")
//    println("Max is: ${sketch.maxValue}")
//    println("Sketch size: ${    sketch.toByteArray().size} bytes")
//
//    println("Distinct count: ${sketch1.estimate}")
}