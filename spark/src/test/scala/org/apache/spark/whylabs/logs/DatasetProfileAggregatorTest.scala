package org.apache.spark.whylabs.logs

import java.time.Instant

import com.whylabs.logging.core.data.InferredType
import org.scalatest.matchers.should.Matchers._

case class ExamplePoint(x: Int, y: Double, z: String)

class DatasetProfileAggregatorTest extends SparkTestingSpec {
  "dataset profile aggregator" should "succeed" in {
    val _spark = spark
    import _spark.implicits._
    val datasetProfileAggregator = DatasetProfileAggregator("test", Instant.now())
    val numberOfEntries = 1000 * 1000

    val examples = (1 to numberOfEntries)
      .map(i => ExamplePoint(i, i * 1.50, "text " + i))
      .toDS()
      .repartition(32) // repartitioning forces Spark to serialize/deserialize data
      .toDF() // have to convert to a DataFrame, aka Dataset[Row]
      .repartition(32)

    // run the aggregation
    val profiles = examples.select(datasetProfileAggregator.toColumn).collect()
    assert(profiles.length == 1)

    val summary = profiles(0).toSummary
    assert(summary.getColumnsMap.size() == 3)

    // assert count
    assert(summary.getColumnsMap.get("x").getCounters.getCount == numberOfEntries)
    assert(summary.getColumnsMap.get("y").getCounters.getCount == numberOfEntries)
    assert(summary.getColumnsMap.get("z").getCounters.getCount == numberOfEntries)

    // assert various data type count
    assert(summary.getColumnsMap.get("x").getSchema.getInferredType.getType == InferredType.Type.INTEGRAL)
    assert(summary.getColumnsMap.get("y").getSchema.getInferredType.getType == InferredType.Type.FRACTIONAL)
    assert(summary.getColumnsMap.get("z").getSchema.getInferredType.getType == InferredType.Type.STRING)
  }
}
