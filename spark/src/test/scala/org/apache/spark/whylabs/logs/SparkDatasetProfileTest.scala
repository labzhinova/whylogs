package org.apache.spark.whylabs.logs

import java.time.Instant

import com.whylabs.logging.core.DatasetProfile
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite


class SparkDatasetProfileTest extends AnyFunSuite {
  test("spark UDT") {
    val spark = SparkSession.builder().master("local[*]").getOrCreate()
    try {
      import spark.implicits._
      val profileDs = (1 to 100)
        .map(i => new DatasetProfile(s"tes-$i", Instant.now()))
        .map(ds => SparkDatasetProfile(ds))
        .toDS()
      val endsWithZeros = profileDs.repartition(32)
        .filter(ds => {
          ds.profile.getName.endsWith("0")
        })
        .map(ds => ds.profile.getName)
        .count()
      assert(endsWithZeros == 10)
    } finally {
      spark.close()
    }
  }
}
