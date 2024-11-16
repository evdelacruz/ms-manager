package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.Context
import com.dfl.contest.exchanger.service.infrastructure.CurrenciesOps.loadCachedRate
import com.dfl.contest.exchanger.service.transactions.TransactionOps.{loadAll, lookup, add => save}
import com.dfl.contest.exchanger.service.transactions.TransactionTypeOps.{load => loadTransactionType}
import com.dfl.contest.exchanger.service.transactions.datasource.domain.Transactions.TransactionType
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.Transactions.DefaultCriteria
import com.dfl.contest.exchanger.service.transactions.datasource.to.Transactions._
import com.dfl.contest.exchanger.service.trial
import com.dfl.seed.akka.base.error.ErrorCode.INVALID_TRANSACTION_TYPE
import com.dfl.seed.akka.base.error.RootException
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.facade.{PagedList, Result, perform, performTry}
import spray.json.JsValue

import java.time.Instant.now
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object TransactionFacade {
  private val Key = "facade-transactionfacade"

  def convert(req: ConversionRequestTO)(implicit context: Context, logger: String = s"$Key#add"): Future[Result[TransactionTO]] = performTry {
    loadCachedRate(req.fromCurrency, req.toCurrency)
      .flatMapConcat(trial(rate => getTransactionType(req.transactionType).flatMapConcat(trial(`type` => single((req, `type`, now, rate, context.authentication.userId)).via(save)))))
  }

  def search(implicit context: Context, logger: String = s"$Key#search"): Future[Result[PagedList[TransactionTO]]] = perform {
    single(DefaultCriteria(context))
      .via(loadAll)
      .fold(Seq[TransactionTO]())(_ :+ _)
      .map(transactions => Result(PagedList(transactions, transactions.size)))
      .orElse(single(Result(PagedList(Seq[TransactionTO](), 0))))
  }

  def summary(implicit context: Context, logger: String = s"$Key#summary"): Future[Result[JsValue]] = perform {
    lookup(DefaultCriteria(context))
      .map(Result(_))
  }

  private def getTransactionType(id: String): Source[Try[TransactionType], NotUsed] = single(id)
    .via(loadTransactionType)
    .map(`type` => `type`._id match {
      case Some(id) => Success(TransactionType(id, `type`.name))
      case None => Failure(RootException(INVALID_TRANSACTION_TYPE, s"The transaction type ID '$id' is invalid. Please provide a valid transaction type."))
    })
    .orElse(single(Failure(RootException(INVALID_TRANSACTION_TYPE, s"The transaction type ID '$id' is invalid. Please provide a valid transaction type."))))
}
