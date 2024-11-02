package com.dfl.contest.exchanger.service.infrastructure.datasource.to

import spray.json.DefaultJsonProtocol._
import spray.json._

object ExchangeRates {

  case class CurrenciesTO(supportedCurrencies: Seq[String])

  //<editor-fold desc="SerDes">

  implicit val CurrenciesTOFormat: RootJsonFormat[CurrenciesTO] = jsonFormat1(CurrenciesTO)

  //</editor-fold>
}
