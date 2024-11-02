package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.ExchangeRates.CurrenciesTO
import com.dfl.contest.exchanger.service.performRequest
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import java.time.Instant
import scala.util.{Failure, Success, Try}

object ExchangeRateOps {

  def loadCurrencies: Source[Try[CurrenciesTO], NotUsed] = {
    single(Success(CurrenciesTO(Seq("USD", "EUR"))))
//    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseCurrenciesResponse}
//
//    single("currencies")
//      .via(performRequest)
//      .map(_.map(CurrenciesTO))
  }

  def loadRates(currency: String): Source[Try[ExchangeRate], NotUsed] = {
    currency match {
      case "USD" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("EUR" -> 1.04))))
      case "EUR" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("USD" -> 0.86))))
    }
//    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseLatestResponse}
//
//    single(s"latest?base=$currency")
//      .via(performRequest)
  }
}
