package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.service.trial
import com.dfl.contest.exchanger.service.infrastructure.datasource.CurrenciesSupervisor.{init => initializeSupervisor}
import com.dfl.contest.exchanger.service.infrastructure.datasource.CurrencyExchanger.{init => initializeExchanger}
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Currencies._
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.ExchangeRates.ExchangeRate
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Exchanges.{RateRequest, RatesUpdate}
import com.dfl.seed.akka.base.{Name, System, Timeout, getActor, searchActor}
import com.dfl.seed.akka.base.error._
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeFlow
import com.dfl.seed.akka.stream.base.Types.SafeSource.{future, single}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object CurrenciesOps {
  private val Key = "service-infrastructure-currenciesops"
  private val CurrenciesSupervisor = getActor("currencies-supervisor", initializeSupervisor())

  def setCachedCurrencies(implicit logger: String = s"$Key#set-cached-currencies"): Flow[Seq[String], Try[Seq[(String, Boolean)]], NotUsed] =
    SafeFlow[Seq[String]]
      .map(SupportedCurrenciesUpdate)
      .ask[Try[Seq[(String, Boolean)]]](CurrenciesSupervisor)

  def setCachedRate(implicit logger: String = s"$Key#set-cached-rate"): Flow[ExchangeRate, Try[(String, Boolean)], NotUsed] = {
    SafeFlow[ExchangeRate]
      .flatMapConcat(rate => loadOrCreateExchanger(rate.base).map(_.map((_, rate))))
      .flatMapConcat(trial(tuple => single(RatesUpdate(tuple._2.rates)).ask[(String, Boolean)](tuple._1).map(Success(_))))
  }

  def loadCachedCurrencies(implicit logger: String = s"$Key#load-cached-currencies"): Source[Try[Seq[String]], NotUsed] =
    single(SupportedCurrenciesRequest())
      .ask[Try[Seq[String]]](CurrenciesSupervisor)

  def loadCachedRate(currencyFrom: String, currencyTo: String)(implicit logger: String = s"$Key#load-cached-rates"): Source[Try[Double], NotUsed] =
    if (currencyFrom == currencyTo) single(Success(1)) else loadRate(currencyFrom, currencyTo)

  //<editor-fold desc="Support Functions">

  private def loadOrCreateExchanger(currency: String)(implicit logger: String = s"$Key#load-or-create-exchanger"): Source[Try[ActorRef], NotUsed] =
    future(searchActor(currency, () => initializeExchanger(currency)))
      .map(Success(_))
      .recover(Failure(_))

  private def loadExchanger(currency: String)(implicit logger: String = s"$Key#load-exchanger"): Source[Option[ActorRef], NotUsed] = {
    implicit val dispatcher: ExecutionContextExecutor = System.dispatcher

    future(System.actorSelection(s"akka://$Name/user/$currency")
      .resolveOne()
      .map(Some(_))
      .recover(_ => None))
  }

  private def loadRate(currencyFrom: String, currencyTo: String)(implicit logger: String = s"$Key#load-rate"): Source[Try[Double], NotUsed] = {
    loadExchanger(currencyFrom)
      .flatMapConcat {
        case Some(exchanger) => single(RateRequest(currencyTo)).ask[Try[Double]](exchanger)
        case None => single(Failure(RootException(INVALID_CURRENCY, s"The currency '$currencyFrom' is not supported. Please use one of the supported currencies.")))
      }
  }

  //</editor-fold>
}
