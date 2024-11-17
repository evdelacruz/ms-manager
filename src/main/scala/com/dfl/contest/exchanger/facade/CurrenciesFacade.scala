package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.infrastructure.CurrenciesOps.{loadCachedCurrencies, setCachedCurrencies, setCachedRate}
import com.dfl.contest.exchanger.service.infrastructure.ExchangeRateOps.{loadRates, loadSupportedCurrencies}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.ExchangeRates.CurrenciesTO
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.Types.{SafeFlow, SafeSource}
import com.dfl.seed.akka.stream.base.facade.{Result, perform}

import java.time.Instant
import java.time.Instant.now
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object CurrenciesFacade {
  private val Key = "facade-currenciesfacade"

  def getSupportedCurrencies: Future[Result[CurrenciesTO]] = perform {
    loadCachedCurrencies
      .map {
        case Failure(_) => Result(INTERNAL_ERROR, "Error when trying to get the supported currencies")
        case Success(currencies) => Result(CurrenciesTO(currencies))
      }
  }

  def refreshCurrencies: Flow[Instant, Try[(String, Boolean)], NotUsed] = {
    SafeFlow[Instant]
      .flatMapConcat(_ => getCurrencies.flatMapConcat(trial(SafeSource(_).flatMapConcat(getRates))))
      .flatMapConcat(trial(single(_).via(setCachedRate)))
  }

  //<editor-fold desc="Support Functions">

  private def getCurrencies: Source[Try[Seq[(String, Boolean)]], NotUsed] = loadSupportedCurrencies
    .flatMapConcat(trial(currencies => single(currencies.supportedCurrencies).via(setCachedCurrencies)))

  private def getRates(tuple: (String, Boolean)): Source[Try[ExchangeRate], NotUsed] = tuple match {
    case (currency, true) => loadRates(currency)
    case (currency, false) => single(Success(ExchangeRate(base = currency, timestamp = now, rates = Map())))
  }

  //</editor-fold>
}
