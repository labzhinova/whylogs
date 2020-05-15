package org.apache.spark.whylabs.logs


import java.io.ByteArrayOutputStream

import com.whylabs.logging.core.DatasetProfile
import com.whylabs.logging.core.format.DatasetProfileMessage
import org.apache.spark.sql.types._

@SQLUserDefinedType(udt = classOf[DatasetProfileDataType])
case class SparkDatasetProfile(profile: DatasetProfile)

class DatasetProfileDataType extends UserDefinedType[SparkDatasetProfile] {
  override def sqlType: DataType = BinaryType

  override def pyUDT: String = "org.apache.spark.whylabs.logs.DatasetProfileDataType"

  override def serialize(obj: SparkDatasetProfile): Array[Byte] = {
    val msg = obj.profile.toProtobuf.build()
    val bos = new ByteArrayOutputStream(msg.getSerializedSize)
    msg.writeTo(bos)
    bos.toByteArray
  }

  override def deserialize(datum: Any): SparkDatasetProfile = {
    datum match {
      case values: Array[Byte] =>
        val profile = DatasetProfile.fromProtobuf(DatasetProfileMessage.parseFrom(values))
        SparkDatasetProfile(profile)
    }
  }

  override def userClass: Class[SparkDatasetProfile] = classOf[SparkDatasetProfile]

  private[spark] override def asNullable: DatasetProfileDataType = this
}
