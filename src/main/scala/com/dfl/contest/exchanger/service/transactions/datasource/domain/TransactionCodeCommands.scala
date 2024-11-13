package com.dfl.contest.exchanger.service.transactions.datasource.domain

import java.time.Instant

object TransactionCodeCommands {

  sealed trait TransactionCodeCommand

  case class UpdateSuffix(lastCode: String) extends TransactionCodeCommand

  case class CodeRequest(date: Instant) extends TransactionCodeCommand
}
