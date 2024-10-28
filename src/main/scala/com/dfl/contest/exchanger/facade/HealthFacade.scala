package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.Sink.head
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service.infrastructure.EnvironmentOps.loadMongoEnvironment
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.HealthStatuses.HealthStatus
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.HealthStatuses.Type._
import com.dfl.seed.akka.base.System
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import scala.concurrent.Future

object HealthFacade {
  private val Name = getOrDefault(this.getClass.getPackage.getImplementationTitle, "-")
  private val Version = getOrDefault(this.getClass.getPackage.getImplementationVersion, "0.0.0")

  def getHealthStatus: Future[HealthStatus] =
    (getInfo zip getDatasource)
      .map(tuple => HealthStatus(OK, None, tuple._2, tuple._1))
      .runWith(head)

  //<editor-fold desc="Support Functions">

  private def getInfo: Source[Map[String, String], NotUsed] = single(Map("name" -> Name, "version" -> Version))

  private def getDatasource: Source[Option[Map[String, Type]], NotUsed] =
    getMongoStatus
      .map(status => Some(Map("mongo" -> status)))

  private def getMongoStatus: Source[Type, NotUsed] =
    loadMongoEnvironment
      .map(_.map(_ => OK).getOrElse(ERROR))
      .recover(_ => ERROR)

  private def getOrDefault(value: String, defaultValue: String): String = if (null != value) value else defaultValue

  //</editor-fold>
}
