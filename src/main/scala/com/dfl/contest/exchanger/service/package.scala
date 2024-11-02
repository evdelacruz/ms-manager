package com.dfl.contest.exchanger

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.seed.akka.base.GlobalConfig
import com.dfl.seed.akka.base.System.dispatcher
import com.dfl.seed.akka.base._
import com.dfl.seed.akka.stream.base.Types.SafeFlow
import com.dfl.seed.akka.stream.base.Types.SafeSource.future
import com.dfl.seed.akka.stream.mongodb.bson.getDefaultSerializationSettings
import org.bson.json.JsonWriterSettings

package object service {
  val EnvironmentName: String = GlobalConfig.getString("application.env")
  val BsonSerializationSettings: JsonWriterSettings = getDefaultSerializationSettings
  private[service] val RaterUri: String = "https://concurso.dofleini.com/exchange-rate/api"

  //<editor-fold desc="Exceptions">

//  case class ValidationException(obj: HsObject, message: String) extends Exception(message)

//  case class RejectionException(detail: HsBatchError) extends Exception(detail.message)

  case class UnexpectedBehaviorException(message: String) extends Exception(message)

  case class UnsupportedCurrencyException(message: String) extends Exception(message)
  object UnsupportedCurrencyException {
    def apply(currency: String): UnsupportedCurrencyException =
      new UnsupportedCurrencyException(s"The currency '$currency' is not supported. Please use one of the supported currencies: .")
  }

  //</editor-fold>

  //<editor-fold desc="Functions">

  private[service] def getResponse[E](getEntity: (StatusCode, String) => E, timeout: Int = 10): HttpResponse => Source[E, NotUsed] = res => {
    import scala.concurrent.duration._

    future(res.entity
      .toStrict(timeout.seconds)
      .map(_.data.utf8String))
      .map(getEntity(res.status, _))
  }

  private[service] def performRequest[E, R](implicit getRequest: E => HttpRequest, parseResponse: HttpResponse => Source[R, NotUsed]): Flow[E, R, NotUsed] = SafeFlow[E]
    .map(getRequest)
    .flatMapConcat(req => future(Http().singleRequest(req)).flatMapConcat(parseResponse))

  //</editor-fold>
}
