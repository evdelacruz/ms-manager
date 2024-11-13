package com.dfl.contest.exchanger.service.transactions

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dfl.contest.exchanger.service.transactions.datasource.TransactionCoder.{init => initializeCoder}
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionCodeCommands.CodeRequest
import com.dfl.seed.akka.base.{getActor, Timeout}
import com.dfl.seed.akka.stream.base.Types.SafeFlow

import java.time.Instant

object TransactionOps {
  private val Key = "service-transactions-transactiontypeops"
  private val Coder = getActor("coder", initializeCoder())

  def getCode: Flow[Instant, String, NotUsed] = SafeFlow[Instant]
    .map(CodeRequest)
    .ask[String](Coder)
}
