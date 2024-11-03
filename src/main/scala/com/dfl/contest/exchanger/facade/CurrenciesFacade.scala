package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.configuration.DefaultContext.CurrenciesSupervisor
import com.dfl.contest.exchanger.service.infrastructure.ExchangeOps.loadOrCreateExchanger
import com.dfl.contest.exchanger.service.infrastructure.ExchangeRateOps.{loadCurrencies, loadRates}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Currencies.{SupportedCurrenciesRequest, SupportedCurrenciesUpdate}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Exchanges.RatesUpdate
import com.dfl.contest.exchanger.service.infrastructure.datasource.to.ExchangeRates.CurrenciesTO
import com.dfl.seed.akka.base.Timeout
import com.dfl.seed.akka.base.error.Error
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.facade.{Result, perform}
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.Types.{SafeFlow, SafeSource}

import java.time.Instant
import java.time.Instant.now
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object CurrenciesFacade {

  def getSupportedCurrencies: Future[Result[CurrenciesTO]] = perform {
    single(SupportedCurrenciesRequest())
      .ask[Try[Seq[String]]](CurrenciesSupervisor)
      .map {
        case Failure(_) => Left(Error(INTERNAL_ERROR, "Error when trying to get the supported currencies"))
        case Success(currencies) => Right(Some(CurrenciesTO(currencies)))
      }
  }

  def refreshCurrencies: Flow[Instant, Try[(String, Boolean)], NotUsed] = {
    SafeFlow[Instant]
      .flatMapConcat(_ => getCurrencies.flatMapConcat {
        case Failure(ex) => single(Failure(ex))
        case Success(tuples) => SafeSource(tuples).flatMapConcat(getRates)
      })
      .flatMapConcat {
        case Failure(ex) => single(Failure(ex))
        case Success(rate) => loadOrCreateExchanger(rate.base).map(_.map((_, rate)))
      }
      .flatMapConcat {
        case Failure(ex) => single(Failure(ex))
        case Success(tuple) => single(RatesUpdate(tuple._2.rates)).ask[(String, Boolean)](tuple._1).map(Success(_))
      }
  }

  //<editor-fold desc="Support Functions">

  private def getCurrencies: Source[Try[Seq[(String, Boolean)]], NotUsed] = loadCurrencies
    .flatMapConcat {
      case Failure(ex) => single(Failure(ex))
      case Success(currencies) => single(SupportedCurrenciesUpdate(currencies.supportedCurrencies)).ask[Try[Seq[(String, Boolean)]]](CurrenciesSupervisor)
    }

  private def getRates(tuple: (String, Boolean)): Source[Try[ExchangeRate], NotUsed] = tuple match {
    case (currency, true) => loadRates(currency)
    case (currency, false) => single(Success(ExchangeRate(base = currency, timestamp = now, rates = Map())))
  }

  //</editor-fold>
}
