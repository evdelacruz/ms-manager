package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.Sink.head
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Source.{combine => combineSources}
import com.dfl.contest.exchanger.service.infrastructure.CurrenciesOps
import com.dfl.contest.exchanger.service.infrastructure.CurrenciesOps.loadCachedCurrencies
import com.dfl.contest.exchanger.service.infrastructure.EnvironmentOps.loadMongoEnvironment
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.HealthStatuses.HealthStatus
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.HealthStatuses.Type._
import com.dfl.seed.akka.base.System
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HealthFacade {
  private val Name = Option(this.getClass.getPackage.getImplementationTitle).getOrElse("-")
  private val Version = Option(this.getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

  def getHealthStatus: Future[HealthStatus] =
    (getInfo zip getDatasource zip getServices)
      .map(tuple => {
        val ((info, datasource), services) = tuple
        HealthStatus(OK, services, datasource, info)
      })
      .runWith(head)

  //<editor-fold desc="Support Functions">

  private def getInfo: Source[Map[String, String], NotUsed] = single(Map("name" -> Name, "version" -> Version))

  private def getDatasource: Source[Option[Map[String, Type]], NotUsed] =
    loadMongoEnvironment
      .map(_.map(_ => OK).getOrElse(ERROR))
      .recover(_ => ERROR)
      .map(status => Some(Map("mongo" -> status)))

  private def getServices: Source[Option[Map[String, Type]], NotUsed] = loadCachedCurrencies
    .map {
      case Success(currencies) if currencies.nonEmpty => OK
      case _ => ERROR
    }
    .map(status => Some(Map("exchange-rate" -> status)))

  //</editor-fold>
}
