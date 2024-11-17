package com.dfl.contest.exchanger.service.infrastructure.datasource

import akka.actor.{Actor, ActorLogging, Props}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Currencies.{SupportedCurrenciesRequest, SupportedCurrenciesUpdate}

import scala.util.{Failure, Success}

class CurrenciesSupervisor extends Actor with ActorLogging {
  import context._

  override def receive: Receive = handle(Seq())

  private def handle(currencies: Seq[(String, Boolean)]): Receive = {
    case SupportedCurrenciesRequest() => sender() ! Success(currencies.filter(_._2).map(_._1))
    case SupportedCurrenciesUpdate(incomingCurrencies) =>
      val updatedCurrencies = incomingCurrencies.map((_, true)).concat(currencies.filterNot(tuple => incomingCurrencies.contains(tuple._1)).map(_.copy(_2 = false)))
      become(handle(updatedCurrencies))
      sender() ! Success(updatedCurrencies)
    case unknown =>
      log.warning("Received unexpected command: {}", unknown)
      sender() ! Failure(new UnsupportedOperationException("Unsupported command"))
  }
}

object CurrenciesSupervisor {
  def init():Props = Props(new CurrenciesSupervisor())
}
