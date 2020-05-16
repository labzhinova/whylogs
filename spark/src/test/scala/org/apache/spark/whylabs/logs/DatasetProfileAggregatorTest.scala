package org.apache.spark.whylabs.logs

import java.time.Instant

import com.whylabs.logging.core.data.InferredType
import org.scalatest.funsuite.AnyFunSuite

case class ExamplePoint(x: Int, y: Double, z: String)

class DatasetProfileAggregatorTest extends AnyFunSuite with SharedSparkContext {
  test("dataset profile aggregator with select() succeeds") {
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

    val summary = profiles(0).value.toSummary
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

  test("dataset profile aggregator with groupBy().agg() succeeds") {
    val _spark = spark
    import _spark.implicits._
    val datasetProfileAggregator = DatasetProfileAggregator("test", Instant.now())
    val numberOfEntries = 1000 * 1000

    val examples = (1 to numberOfEntries)
      // the "x" field value is between 0 to 3 (inclusive)
      .map(i => ExamplePoint(i % 4, i * 1.50, "text " + i))
      .toDS()
      .repartition(32) // repartitioning forces Spark to serialize/deserialize data
      .toDF() // have to convert to a DataFrame, aka Dataset[Row]
      .repartition(32)

    import org.apache.spark.sql.functions._

    // group by column x and aggregate
    val groupedDf = examples.groupBy(col("x"))
      .agg(datasetProfileAggregator.toColumn.name("whylogs_profile"))
    groupedDf.printSchema()

    // extract the nested column, collect them and turn them into Summary objects
    val summaries = groupedDf.select("whylogs_profile.value")
      .collect()
      .map(_.getAs[ScalaDatasetProfile](0))
      .map(_.value)
      .map(_.toSummary)
    assert(summaries.length == 4)

    // total number of counts should be the total number of entries
    assert(summaries.map(_.getColumnsOrThrow("x")).map(_.getCounters.getCount).sum == numberOfEntries)
    assert(summaries.map(_.getColumnsOrThrow("y")).map(_.getCounters.getCount).sum == numberOfEntries)
    assert(summaries.map(_.getColumnsOrThrow("z")).map(_.getCounters.getCount).sum == numberOfEntries)

    // verify the max and min in each summary should be the same for the "x" column
    // remember, we are grouping by x
    summaries.foreach(s => {
      val numberSummary = s.getColumnsMap.get("x").getNumberSummary
      assert(numberSummary.getMax == numberSummary.getMin)

      // assert that each group should have equal number of entries
      assert(numberSummary.getCount == numberOfEntries / 4)
    })
  }

}
