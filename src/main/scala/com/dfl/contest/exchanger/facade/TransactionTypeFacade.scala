package com.dfl.contest.exchanger.facade

import com.dfl.contest.exchanger.Context
import com.dfl.contest.exchanger.service.ValidationException
import com.dfl.contest.exchanger.service.transactions.TransactionTypeOps.{load, loadAll, remove, add => save, update => modify}
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.TransactionTypes.DefaultCriteria
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes.TransactionType
import com.dfl.contest.exchanger.service.transactions.datasource.to.TransactionTypes.TransactionTypeTO
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.facade.{PagedList, Result, perform}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object TransactionTypeFacade {
  private val Key = "facade-currenciesfacade"

  def add(to: TransactionTypeTO)(implicit logger: String = s"$Key#get"): Future[Result[TransactionType]] = perform {
    single(to)
      .via(save)
      .map {
        case Failure(ValidationException(error)) => Result(error.code, error.detail)
        case Success(value) => Result(value)
      }
  }

  def update(id: String, to: TransactionTypeTO)(implicit logger: String = s"$Key#get"): Future[Result[TransactionTypeTO]] = perform {
    single((id, to))
      .via(modify)
      .map {
        case Failure(ValidationException(error)) => Result(error.code, error.detail)
        case Success(value) => Result(value)
      }
  }

  def delete(id: String)(implicit logger: String = s"$Key#get"): Future[Result[TransactionType]] = perform {
    single(id)
      .via(remove)
      .map(_ => Result())
  }

  def get(id: String)(implicit logger: String = s"$Key#get"): Future[Result[TransactionType]] = perform {
    single(id)
      .via(load)
      .map(Result(_))
      .orElse(single(Result(INVALID_REFERENCE, "Entity not found")))
  }

  def search(implicit context: Context, logger: String = s"$Key#search"): Future[Result[PagedList[TransactionType]]] = perform {
    single(DefaultCriteria(context))
      .via(loadAll)
      .fold(Seq[TransactionType]())(_ :+ _)
      .map(types => Result(PagedList(types, types.size)))
      .orElse(single(Result(PagedList(Seq(), 0))))
  }
}
