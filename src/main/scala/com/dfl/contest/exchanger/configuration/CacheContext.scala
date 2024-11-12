package com.dfl.contest.exchanger.configuration

import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.server.{Directive, Directive0, RequestContext, Route, RouteResult}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.server.directives.CachingDirectives._
import com.dfl.seed.akka.base.System
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

object CacheContext {
  private val DefaultCachingSettings = CachingSettings(System)

  private val LfuCacheSettings = DefaultCachingSettings.lfuCacheSettings
    .withInitialCapacity(100)
    .withMaxCapacity(5000)
    .withTimeToLive(20.seconds)
    .withTimeToIdle(20.seconds)

  private val SystemCache: Cache[String, RouteResult] = LfuCache(DefaultCachingSettings.withLfuCacheSettings(LfuCacheSettings))

  private val Keyer: PartialFunction[RequestContext, Future[String]] = {
    case ctx: RequestContext => getKey(ctx.request)
  }

  def cacheable(route: => Route): Route = customCache(SystemCache, Keyer)(route)

  //<editor-fold desc="Support Functions">

  private def customCache[K](cache: Cache[K, RouteResult], keyer: PartialFunction[RequestContext, Future[K]]): Directive0 =
    Directive { inner => ctx =>
      import ctx.executionContext
      keyer.lift(ctx) match {
        case Some(future) => future.flatMap(cache.apply(_, () => inner(())(ctx)))
        case None         => inner(())(ctx)
      }
    }

  private def getKey(req: HttpRequest): Future[String] = {
    import com.dfl.seed.akka.base.System.dispatcher

    req.entity.toStrict(5.seconds).map(_.data.utf8String.parseJson match {
      case JsObject(fields) if fields.contains("fromCurrency") && fields.contains("toCurrency") && fields.contains("amount") && fields.contains("transactionType") =>
        fields.toSeq.sortBy(_._1).map(getPartialKey).mkString("-")
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
