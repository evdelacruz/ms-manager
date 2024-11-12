package com.dfl.contest.exchanger.dist.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{Segment => pvar, _}
import akka.http.scaladsl.server.Route
import com.dfl.contest.exchanger.configuration.SecurityContext.routesc
import com.dfl.contest.exchanger.facade.TransactionTypeFacade.{add => addTransactionType, get => getTransactionType, delete => deleteTransactionType, update => updateTransactionType, search => searchTransactionTypes}
import com.dfl.contest.exchanger.service.transactions.datasource.to.TransactionTypes.{TransactionTypeCreateTO, TransactionTypeUpdateTO}
import com.dfl.seed.akka.http.Routing.{create, resolve}

object TransactionTypeRoute {

  val Routes: Route = routesc { implicit context =>
    path("settings" / "transactions-types") {
      get {
        resolve(searchTransactionTypes)
      } ~ post {
        entity(as[TransactionTypeCreateTO]) { to =>
          create(addTransactionType(to))
        }
      }
    } ~ path("settings" / "transactions-types" / pvar) { id =>
      get {
        resolve(getTransactionType(id))
      } ~ put {
        entity(as[TransactionTypeUpdateTO]) { to =>
          resolve(updateTransactionType(to.copy(id = Some(id))))
        }
      } ~ delete {
        resolve(deleteTransactionType(id))
      }
    }
  }
}
