package org.apache.spark.whylabs.logs

import java.time.Instant

import com.whylabs.logging.core.DatasetProfile
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.BoundReference
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Encoder, Encoders, Row}

import scala.reflect.ClassTag

/**
 * A dataset aggregator. It aggregates [[Row]] into DatasetProfile objects
 * underneath the hood.
 *
 * @param name      the name of the dataset
 * @param timestamp the timestamp
 */
case class DatasetProfileAggregator(name: String, timestamp: Instant)
  extends Aggregator[Row, DatasetProfile, ScalaDatasetProfile] with Serializable {
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

  override def finish(reduction: DatasetProfile): ScalaDatasetProfile = {
    ScalaDatasetProfile(reduction)
  }

  override def bufferEncoder: Encoder[DatasetProfile] = Encoders.javaSerialization(classOf[DatasetProfile])

  /**
   * To understand the detailed implementation of this class, see  [[ExpressionEncoder]].
   *
   * We use some internal Spark API here.
   */
  override def outputEncoder: Encoder[ScalaDatasetProfile] = {
    val dataType = ScalaDatasetProfileUDT()
    val structType = new StructType().add("value", dataType)

    // based on ExpressionEncoder
    // TODO: understand why we can't directly use use the 'dataType' object here
    // but have to refer to the reflection logic (it returns an ObjectType)
    val reflectionType = ScalaReflection.dataTypeFor[ScalaDatasetProfile]
    val inputObject = BoundReference(0, reflectionType, nullable = true)
    val serializer = ScalaReflection.serializerFor[ScalaDatasetProfile](inputObject)
    val deserializer = ScalaReflection.deserializerFor[ScalaDatasetProfile]

    new ExpressionEncoder[ScalaDatasetProfile](
      structType,
      flat = false,
      serializer.flatten,
      deserializer,
      ClassTag(dataType.userClass)
    )
  }
}
