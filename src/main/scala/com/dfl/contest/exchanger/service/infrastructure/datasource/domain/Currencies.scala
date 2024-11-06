package com.dfl.contest.exchanger.service.infrastructure.datasource.domain

object Currencies {

  sealed trait CurrenciesSupervisionCommand

  private[infrastructure] case class SupportedCurrenciesRequest() extends CurrenciesSupervisionCommand

  private[infrastructure] case class SupportedCurrenciesUpdate(currencies: Seq[String]) extends CurrenciesSupervisionCommand
}
