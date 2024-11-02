package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.infrastructure.CurrencyExchanger.init
import com.dfl.seed.akka.base.{Name, System, Timeout, searchActor}
import com.dfl.seed.akka.stream.base.Types.SafeSource.future

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object ExchangeOps {

  def loadOrCreateExchanger(currency: String): Source[Try[ActorRef], NotUsed] = future(searchActor(currency, () => init(currency)))
    .map(Success(_))
    .recover(Failure(_))

  def loadExchanger(currency: String): Source[Try[ActorRef], NotUsed] = {
    implicit val dispatcher: ExecutionContextExecutor = System.dispatcher

    future(System.actorSelection(s"akka://$Name/user/$currency")
      .resolveOne()
      .map(Success(_))
      .recover(_ => Failure(UnsupportedCurrencyException(currency))))
  }

}
