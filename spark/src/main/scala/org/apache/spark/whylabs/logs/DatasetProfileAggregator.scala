package org.apache.spark.whylabs.logs

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}

import com.whylabs.logging.core.DatasetProfile
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.BoundReference
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{Encoder, Encoders, Row}

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

object InstantDateTimeFormatter {
  private val Formatter = DateTimeFormatter.BASIC_ISO_DATE.withZone(ZoneOffset.UTC)

  def format(instant: Instant): String = {
    Formatter.format(instant)
  }
}

/**
 * A dataset aggregator. It aggregates [[Row]] into DatasetProfile objects
 * underneath the hood.
 *
 * @param datasetName the name of the dataset
 * @param tsEpochMillis   the epoch time in Millis
 */
case class DatasetProfileAggregator(datasetName: String,
                                    tsEpochMillis: Long,
                                    timeColumn: String = null,
                                    groupByColumns: Seq[String] = Seq())
  extends Aggregator[Row, DatasetProfile, ScalaDatasetProfile] with Serializable {
  private val allGroupByColumns = (groupByColumns ++ Option(timeColumn).toSeq).toSet

  override def zero: DatasetProfile = new DatasetProfile("", Instant.ofEpochMilli(0))

  override def reduce(profile: DatasetProfile, row: Row): DatasetProfile = {
    val schema = row.schema

    val timeStamp = if (timeColumn != null) {
      // extract timestamp from the data
      row.getTimestamp(schema.fieldIndex(timeColumn)).toInstant
    } else {
      // use the aggregator timestamp
      Instant.ofEpochMilli(tsEpochMillis)
    }

    val timestampStr = InstantDateTimeFormatter.format(timeStamp)
    val sortedTags = (groupByColumns
      .map(schema.fieldIndex)
      .map(row.get)
      .map(_.toString) :+ timestampStr
      ).sorted
    val timedProfile = if (profile.getTimestamp.toEpochMilli == 0 && timeStamp.toEpochMilli > 0) {
      // we have an empty profile
      new DatasetProfile("", timeStamp, sortedTags.asJava)
    } else {
      // profile already saw data. Make sure the timestamp matches
      if (timeStamp != profile.getTimestamp) {
        throw new IllegalStateException(s"Mismatched timestamp. Previously seen ts: [${profile.getTimestamp}]. Current timestamp: $timeStamp")
      }

      // ensure tags match
      if (profile.getTags != sortedTags.asJava) {
        throw new IllegalStateException(s"Mismatched grouping columns. Previously seen values: ${profile.getTags}. Current values: ${sortedTags.asJava}")
      }

      profile
    }

    // TODO: we have the schema here. Support schema?
    for (field: StructField <- schema) {
      if (!allGroupByColumns.contains(field.name)) {
        timedProfile.track(field.name, row.get(schema.fieldIndex(field.name)))
      }
    }

    timedProfile
  }

  override def merge(profile1: DatasetProfile, profile2: DatasetProfile): DatasetProfile = {
    if (profile1.getColumns.isEmpty) return profile2
    if (profile2.getColumns.isEmpty) return profile1
    profile1.merge(profile2)
  }

  override def finish(reduction: DatasetProfile): ScalaDatasetProfile = {
    val finalProfile = new DatasetProfile(
      datasetName,
      reduction.getTimestamp,
      reduction.getTags,
      reduction.getColumns
    )
    ScalaDatasetProfile(finalProfile)
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
