package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.facade.HealthFacade.getHealthStatus

import scala.util.{Failure, Success}

object HealthcheckRoute {

  val Routes: Route = pathPrefix("healthcheck") {
    pathEnd {
      get {
        complete(OK)
      }
    } ~ pathPrefix("status") {
      pathEnd {
        get {
          onComplete(getHealthStatus) {
            case Failure(ex) => complete(InternalServerError, ex.getMessage)
            case Success(response) => complete(OK, response)
          }
        }
      }
    }
  }
}
