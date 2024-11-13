package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.configuration.CacheContext.cacheable
import com.dfl.contest.exchanger.configuration.SecurityContext.routesc
import com.dfl.contest.exchanger.facade.TransactionFacade.{add => addTransaction}
import com.dfl.contest.exchanger.service.transactions.datasource.to.Transactions.ConversionRequestTO
import com.dfl.seed.akka.http.Routing.create

object TransactionRoute {

  val Routes: Route = routesc { implicit context =>
    pathPrefix("conversion") {
      cacheable {
        post {
          entity(as[ConversionRequestTO]) { to =>
            //create(addTransaction(to))
            ???
          }
        }
      }
    } ~ pathPrefix("transactions") {
      get {
        ???
      }
    } ~ pathPrefix("statistics") {
      get {
        ???
      }
    }
  }
}
