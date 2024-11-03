package com.dfl.contest.exchanger.service.infrastructure.datasource

import akka.actor.{Actor, ActorLogging, Props}
import com.dfl.contest.exchanger.service.UnsupportedCurrencyException
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Exchanges.{RateRequest, RatesUpdate}

import scala.util.{Failure, Success}

class CurrencyExchanger(id: String) extends Actor with ActorLogging {
  import context._

  override def receive: Receive = handle(Map())

  private def handle(rates: Map[String, Double]): Receive = {
    case RateRequest(currency) => rates.get(currency) match {
      case Some(rate) => sender() ! Success(rate)
      case None => sender() ! Failure(UnsupportedCurrencyException(currency))
    }
    case RatesUpdate(updatedRates) =>
      become(handle(updatedRates))
      sender() ! (id, true)
  }
}

object CurrencyExchanger {
  def init(id: String): Props = Props(new CurrencyExchanger(id))
}
