package org.apache.spark.whylabs.logs

import java.time.LocalDateTime

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec

abstract class SparkTestingSpec extends AnyFlatSpec with BeforeAndAfter {
  var spark: SparkSession = _
  var sc: SparkContext = _

  before {
    val builder = SparkSession.builder()
      .master("local[*, 3]")
      .appName("SparkTesting-" + LocalDateTime.now().toString)

    spark = builder.getOrCreate()
    sc = spark.sparkContext
  }

  after {
    spark.stop()
  }

  "SparkSession" should "be active" in {
    assert(spark != null)
    assert(!sc.isStopped)
    assert(sc.isLocal)
  }
}
