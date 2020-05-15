package org.apache.spark.whylabs.logs

import java.time.Instant

import com.whylabs.logging.core.DatasetProfile
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders, Row}

case class DatasetProfileAggregator(name: String, timestamp: Instant)
  extends Aggregator[Row, DatasetProfile, DatasetProfile] with Serializable {
  override def zero: DatasetProfile = new DatasetProfile(name, timestamp)

  override def reduce(profile: DatasetProfile, row: Row): DatasetProfile = {
    val schema = row.schema
    // TODO: we have the schema here. Support schema?
    for (elem <- schema) {
      profile.track(elem.name, row.get(schema.fieldIndex(elem.name)))
    }
    profile
  }

  override def merge(profile1: DatasetProfile, profile2: DatasetProfile): DatasetProfile = {
    profile1.merge(profile2)
  }

  override def finish(reduction: DatasetProfile): DatasetProfile = {
    reduction
  }

  override def bufferEncoder: Encoder[DatasetProfile] = Encoders.javaSerialization(classOf[DatasetProfile])

  override def outputEncoder: Encoder[DatasetProfile] = Encoders.javaSerialization(classOf[DatasetProfile])
}
