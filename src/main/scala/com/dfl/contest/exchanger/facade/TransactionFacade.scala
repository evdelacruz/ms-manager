package com.dfl.contest.exchanger.facade

import com.dfl.contest.exchanger.Context
import com.dfl.contest.exchanger.service.transactions.TransactionOps.getCode
import com.dfl.contest.exchanger.service.transactions.datasource.to.Transactions._
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.facade.{PagedList, Result, perform, performTry}

import java.time.Instant
import scala.concurrent.Future

object TransactionFacade {
  private val Key = "facade-transactionfacade"

  def add(to: ConversionRequestTO)(implicit logger: String = s"$Key#add"): Future[Result[TransactionTO]] = perform {
    ???
  }

  def search(implicit context: Context, logger: String = s"$Key#search"): Future[Result[PagedList[TransactionTO]]] = perform {
    ???
  }

  def summary(implicit context: Context, logger: String = s"$Key#summary"): Future[Result[TransactionSummaryTO]] = perform {
    ???
  }
}
