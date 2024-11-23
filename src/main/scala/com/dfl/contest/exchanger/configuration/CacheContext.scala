package com.dfl.contest.exchanger.configuration

import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.1`
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server._
import com.dfl.seed.akka.base.System
import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.duration._

object CacheContext {
  private val DefaultCachingSettings = CachingSettings(System)

  private val LfuCacheSettings = DefaultCachingSettings.lfuCacheSettings
    .withInitialCapacity(100)
    .withMaxCapacity(1000000)
    .withTimeToLive(20.seconds)
    .withTimeToIdle(20.seconds)

  private val SystemCache: Cache[String, RouteResult] = LfuCache(DefaultCachingSettings.withLfuCacheSettings(LfuCacheSettings))

  private val Keyer: PartialFunction[RequestContext, Future[String]] = {
    case ctx: RequestContext => getKey(ctx.request)
  }

  private val DefaultErrorResponse = Complete(
    HttpResponse(
      status = BadRequest,
      headers = Seq(),
      entity = HttpEntity(`application/json`, "{\"code\": \"DUPLICATE_TRANSACTION\", \"message\": \"A similar transaction was already processed within the last 20 seconds. Please try again later.\"}"),
      protocol = `HTTP/1.1`
    )
  )

  private val InvolvedFields = "amount-fromCurrency-toCurrency-transactionType"

  //<editor-fold desc="Functions">

  def cacheable(route: => Route): Route = customCache(SystemCache, Keyer)(route)

  private def customCache[K](cache: Cache[K, RouteResult], keyer: PartialFunction[RequestContext, Future[K]]): Directive0 =
    Directive { inner => ctx =>
      import ctx.executionContext
      keyer.lift(ctx) match {
        case Some(future) => future.flatMap(key => cache.get(key).map(_ => successful(DefaultErrorResponse)).getOrElse(cache.apply(key, () => inner(())(ctx))))
        case None         => inner(())(ctx)
      }
    }

  private def getKey(req: HttpRequest): Future[String] = {
    import com.dfl.seed.akka.base.System.dispatcher

    req.entity.toStrict(5.seconds).map(_.data.utf8String.parseJson match {
      case JsObject(fields) => fields.toSeq.filter(tuple => InvolvedFields.contains(tuple._1)).sortBy(_._1).map(getPartialKey).mkString
      case _ => "-"
    })
  }

  private def getPartialKey(tuple: (String, JsValue)): String = tuple match {
    case ("fromCurrency", JsString(currency)) => currency
    case ("toCurrency", JsString(currency)) => currency
    case ("transactionType", JsString(id)) => id
    case ("amount", JsNumber(amount)) => amount.toString
    case _ => "-"
  }

  //</editor-fold>
}
