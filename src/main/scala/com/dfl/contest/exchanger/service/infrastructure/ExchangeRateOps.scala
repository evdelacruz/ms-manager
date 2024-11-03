package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.ExchangeRates.CurrenciesTO
import com.dfl.contest.exchanger.service.performRequest
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import java.time.Instant
import java.time.Instant.now
import scala.util.{Failure, Success, Try}

object ExchangeRateOps {

  def loadCurrencies: Source[Try[CurrenciesTO], NotUsed] = {
//    single(Success( if (now.toEpochMilli % 2 == 0) CurrenciesTO(Seq("USD", "EUR")) else CurrenciesTO(Seq("CUP", "MLC")) ))
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseCurrenciesResponse}

    single("currencies")
      .via(performRequest)
      .map(_.map(CurrenciesTO))
  }

  def loadRates(currency: String): Source[Try[ExchangeRate], NotUsed] = {
//    currency match {
//      case "USD" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("EUR" -> 1.00, "MLC" -> 1.00, "CUP" -> 1.00))))
//      case "EUR" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("USD" -> 2.00, "MLC" -> 2.00, "CUP" -> 2.00))))
//      case "MLC" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("EUR" -> 3.00, "USD" -> 3.00, "CUP" -> 3.00))))
//      case "CUP" => single(Success(ExchangeRate(base = currency, timestamp = Instant.now, rates = Map("EUR" -> 4.00, "MLC" -> 4.00, "USD" -> 4.00))))
//    }
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseLatestResponse}

    single(s"latest?base=$currency")
      .via(performRequest)
  }
}
