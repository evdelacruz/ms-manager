package com.dfl.contest.exchanger.service.infrastructure.datasource.to

import com.dfl.seed.akka.base.JsonProtocol.EnumJsonConverter
import spray.json.DefaultJsonProtocol._
import spray.json._

object HealthStatuses {

  object Type extends Enumeration {
    type Type = Value
    val OK = Value("OK")
    val ERROR = Value("ERROR")
  }

  case class HealthStatus(`type`: Type.Type, services: Option[Map[String, Type.Type]], datasource: Option[Map[String, Type.Type]], info: Map[String, String])

  //<editor-fold desc="SerDes">

  implicit val TypeFormat: EnumJsonConverter[Type.type] = new EnumJsonConverter(Type)

  implicit val HealthStatusFormat: RootJsonFormat[HealthStatus] = jsonFormat4(HealthStatus)

  //</editor-fold>
}
