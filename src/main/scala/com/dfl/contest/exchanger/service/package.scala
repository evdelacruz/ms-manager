package com.dfl.contest.exchanger

import com.dfl.seed.akka.base.GlobalConfig
import com.dfl.seed.akka.stream.mongodb.bson.getDefaultSerializationSettings
import org.bson.json.JsonWriterSettings

package object service {
  val EnvironmentName: String = GlobalConfig.getString("application.env")
  val BsonSerializationSettings: JsonWriterSettings = getDefaultSerializationSettings
}
