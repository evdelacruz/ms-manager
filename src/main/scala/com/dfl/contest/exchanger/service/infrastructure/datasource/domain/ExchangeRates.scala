package com.dfl.contest.exchanger.service.infrastructure.datasource.domain

import akka.NotUsed
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service._
import com.dfl.seed.akka.base.JsonProtocol._
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant
import scala.util.{Failure, Try}

object ExchangeRates {

  case class ExchangeRate(base: String, timestamp: Instant, rates: Map[String, Double])

  //<editor-fold desc="Functions">

  implicit private[hubspot] def getRequest(path: String): HttpRequest = Get().withUri(s"$RaterUri/$path")

  implicit private[hubspot] def parseCurrenciesResponse: HttpResponse => Source[Try[Seq[String]], NotUsed] = getResponse((status, body) => status match {
    case res if res.isSuccess() => Try(body.parseJson.convertTo[Seq[String]])
    case BadRequest => Try(body.parseJson.convertTo[Seq[String]]).recoverWith(ex => Failure(UnexpectedBehaviorException(s"Error '${ex.getMessage}' when trying to parse '$body'")))
    case _ => Failure(UnexpectedBehaviorException(s"$status => $body"))
  })

  implicit private[hubspot] def parseLatestResponse: HttpResponse => Source[Try[ExchangeRate], NotUsed] = getResponse((status, body) => status match {
    case res if res.isSuccess() => Try(body.parseJson.convertTo[ExchangeRate])
    case BadRequest => Try(body.parseJson.convertTo[ExchangeRate]).recoverWith(ex => Failure(UnexpectedBehaviorException(s"Error '${ex.getMessage}' when trying to parse '$body'")))
    case _ => Failure(UnexpectedBehaviorException(s"$status => $body"))
  })

  //</editor-fold>

  //<editor-fold desc="SerDes">

  implicit val ExchangeRateFormat: RootJsonFormat[ExchangeRate] = jsonFormat3(ExchangeRate)

  //</editor-fold>
}
