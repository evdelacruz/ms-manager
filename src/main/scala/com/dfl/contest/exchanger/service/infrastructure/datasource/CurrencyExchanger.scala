package com.dfl.contest.exchanger.service.infrastructure.datasource

import akka.actor.{Actor, ActorLogging, Props}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Exchanges.{RateRequest, RatesUpdate}
import com.dfl.seed.akka.base.error.ErrorCode.INVALID_CURRENCY
import com.dfl.seed.akka.base.error.RootException

import scala.util.{Failure, Success}

class CurrencyExchanger(id: String) extends Actor with ActorLogging {
  import context._

  override def receive: Receive = handle(Map())

  private def handle(rates: Map[String, Double]): Receive = {
    case RateRequest(currency) => rates.get(currency) match {
      case Some(rate) => sender() ! Success(rate)
      case None => sender() ! Failure(RootException(INVALID_CURRENCY, s"The currency '$currency' is not supported. Please use one of the supported currencies."))
    }
    case RatesUpdate(updatedRates) =>
      become(handle(updatedRates))
      sender() ! (id, true)
  }
}

object CurrencyExchanger {
  def init(id: String): Props = Props(new CurrencyExchanger(id))
}
