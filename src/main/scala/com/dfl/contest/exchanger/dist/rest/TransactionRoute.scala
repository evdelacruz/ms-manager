package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.configuration.CacheContext.cacheable
import com.dfl.contest.exchanger.configuration.SecurityContext.routesc
import com.dfl.contest.exchanger.facade.TransactionFacade.{convert, search, summary}
import com.dfl.contest.exchanger.service.transactions.datasource.to.Transactions.ConversionRequestTO
import com.dfl.seed.akka.base.JsonProtocol._
import com.dfl.seed.akka.base.error.ErrorCode.MISSING_REQUIRED_FIELDS
import com.dfl.seed.akka.base.error._
import com.dfl.seed.akka.http.Routing.{create, resolve}
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.util.{Failure, Success, Try}

object TransactionRoute {
  private val DefaultErrorResponse = Error(MISSING_REQUIRED_FIELDS, "The following required fields are missing: 'fromCurrency', 'toCurrency', 'amount', 'transactionType'. Please provide valid values for all required fields.")

  val Routes: Route = routesc { implicit context =>
    pathPrefix("conversion") {
      cacheable {
        post {
          entity(as[JsObject]) { json =>
            Try(json.convertTo[ConversionRequestTO]) match {
              case Failure(_) => complete(BadRequest, DefaultErrorResponse)
              case Success(to) => create(convert(to))
            }
          }
        }
      }
    } ~ pathPrefix("transactions") {
      get {
        resolve(search)
      }
    } ~ pathPrefix("statistics") {
      get {
        resolve(summary)
      }
    }
  }
}
