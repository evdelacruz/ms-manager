package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{Segment => pvar, _}
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.configuration.CacheContext.cacheable
import com.dfl.contest.exchanger.configuration.SecurityContext.routesc
import com.dfl.seed.akka.http.Routing.{create, resolve}

import java.time.Instant

object TransactionRoute {

  val Routes: Route = routesc { implicit context =>
    pathPrefix("conversion") {
      cacheable {
        post {
          complete(OK, Instant.now().toString)
        }
      }
    } ~ pathPrefix("transactions") {
      get {
        ???
      }
    }
  }
}
