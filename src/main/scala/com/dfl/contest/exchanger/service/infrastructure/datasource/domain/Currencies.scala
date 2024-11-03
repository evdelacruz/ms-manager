package com.dfl.contest.exchanger.service.infrastructure.datasource.domain

import java.time.Instant

object Currencies {

  sealed trait CurrenciesSupervisionCommand

  case class SupportedCurrenciesRequest() extends CurrenciesSupervisionCommand

  case class SupportedCurrenciesUpdate(currencies: Seq[String]) extends CurrenciesSupervisionCommand
}
