package com.dfl.contest.exchanger.service.infrastructure.datasource.domain

object Exchanges {

  sealed trait CurrencyExchangerCommand

  case class RateRequest(currency: String) extends CurrencyExchangerCommand

  case class RatesUpdate(rates: Map[String, Double]) extends CurrencyExchangerCommand
}
