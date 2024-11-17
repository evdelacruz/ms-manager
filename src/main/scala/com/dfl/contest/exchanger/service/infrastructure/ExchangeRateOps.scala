package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.ExchangeRates.CurrenciesTO
import com.dfl.contest.exchanger.service.performRequest
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import scala.util.Try

object ExchangeRateOps {
  private val Key = "service-infrastructure-exchangerateops"

  def loadSupportedCurrencies(implicit logger: String = s"$Key#load-currencies"): Source[Try[CurrenciesTO], NotUsed] = {
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseCurrenciesResponse}

    single("currencies")
      .via(performRequest)
      .map(_.map(CurrenciesTO))
  }

  def loadRates(currency: String)(implicit logger: String = s"$Key#load-rates"): Source[Try[ExchangeRate], NotUsed] = {
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseLatestResponse}

    single(s"latest?base=$currency")
      .via(performRequest)
  }
}
