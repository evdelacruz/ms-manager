package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.configuration.SecurityContext.routesc
import com.dfl.contest.exchanger.facade.CurrenciesFacade.getSupportedCurrencies
import com.dfl.seed.akka.http.Routing.resolve

object CurrencyRoute {

  val Routes: Route = routesc { _ =>
    pathPrefix("currencies") {
      get {
        resolve(getSupportedCurrencies)
      }
    }
  }
}
