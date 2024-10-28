package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.performRequest
import com.dfl.seed.akka.stream.base.Types.SafeSource.single

import scala.util.Try

object ExchangeRateOps {

  def loadCurrencies: Source[Try[Seq[String]], NotUsed] = {
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseCurrenciesResponse}

    single("currencies")
      .via(performRequest)
  }

  def loadRates(currency: String): Source[Try[ExchangeRate], NotUsed] = {
    import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.{getRequest, parseLatestResponse}

    single(s"latest?base=$currency")
      .via(performRequest)
  }
}
