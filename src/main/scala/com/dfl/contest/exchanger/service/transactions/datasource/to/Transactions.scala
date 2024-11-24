package com.dfl.contest.exchanger.service.transactions.datasource.to

import com.dfl.contest.exchanger.service.transactions.datasource.domain.Transactions.{Transaction, TransactionType}
import com.dfl.seed.akka.base.JsonProtocol._
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant

object Transactions {

  type ConversionData = (ConversionRequestTO, TransactionType, Instant, Double, String)

  case class ConversionRequestTO(fromCurrency: String, toCurrency: String, amount: Double, transactionType: String)

  case class TransactionTO(transactionId: String, transactionCode: String, fromCurrency: String, toCurrency: String,
                           amount: Double, amountConverted: Double, exchangeRate: Double, transactionType: String,
                           createdAt: Instant, userId: String)

  //<editor-fold desc="Functions">

  def getTransaction(transaction: Transaction): TransactionTO = TransactionTO(
    transactionId = transaction._id.map(_.toHexString).orNull,
    transactionCode = transaction.transactionCode,
    fromCurrency = transaction.fromCurrency,
    toCurrency = transaction.toCurrency,
    amount = transaction.amount,
    amountConverted = transaction.amountConverted,
    exchangeRate = transaction.exchangeRate,
    transactionType = transaction.transactionType.name,
    createdAt = transaction.createdAt,
    userId = transaction.userId
  )

  //</editor-fold>

  //<editor-fold desc="SerDes">

  implicit val ConversionRequestTOFormatter: RootJsonFormat[ConversionRequestTO] = jsonFormat4(ConversionRequestTO)

  implicit val TransactionTOFormatter: RootJsonFormat[TransactionTO] = jsonFormat10(TransactionTO)

  //</editor-fold>
}
