package com.dfl.contest.exchanger.service.transactions.datasource.to

import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant

object Transactions {

  case class ConversionRequestTO(fromCurrency: String, toCurrency: String, amount: BigDecimal, transactionType: String)

  case class TransactionTO(transactionId: String, transactionCode: String, fromCurrency: String, toCurrency: String,
                           amount: BigDecimal, amountConverted: BigDecimal, exchangeRate: BigDecimal, transactionType: String,
                           createdAt: Instant, userId: String)

  case class TransactionSummaryTO(transactionsByType: Map[String, BigDecimal], totalAmountConvertedByCurrency: Map[String, BigDecimal],
                                  totalAmountByTransactionType: Map[String, BigDecimal], averageAmountByTransactionType: Map[String, BigDecimal],
                                  totalTransactions: Int)

  //<editor-fold desc="SerDes">

  implicit val ConversionRequestTOFormatter: RootJsonFormat[ConversionRequestTO] = jsonFormat4(ConversionRequestTO)

  //</editor-fold>
}
