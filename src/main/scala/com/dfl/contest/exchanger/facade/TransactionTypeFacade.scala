package com.dfl.contest.exchanger.facade

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.contest.exchanger.Context
import com.dfl.contest.exchanger.service.encode
import com.dfl.contest.exchanger.service.transactions.TransactionOps.{countByType, updateTypeName}
import com.dfl.contest.exchanger.service.transactions.TransactionTypeOps.{load, loadAll, remove, add => save, update => modify}
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.TransactionTypes.DefaultCriteria
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes._
import com.dfl.contest.exchanger.service.transactions.datasource.to.TransactionTypes._
import com.dfl.seed.akka.base.error._
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeSource.{empty, single}
import com.dfl.seed.akka.stream.base.facade.{PagedList, Result, perform, performTry}

import scala.concurrent.Future
import scala.util.{Failure, Try}

object TransactionTypeFacade {
  private val Key = "facade-transactiontypefacade"

  def add(to: TransactionTypeCreateTO)(implicit logger: String = s"$Key#add"): Future[Result[TransactionTypeTO]] = performTry {
    single(to)
      .via(save)
  }

  def update(to: TransactionTypeUpdateTO)(implicit logger: String = s"$Key#update"): Future[Result[TransactionTypeTO]] = performTry {
    to.id.map(single(_).via(load)).getOrElse(empty)
      .flatMapConcat {
        case tt@TransactionType(Some(id), name, _, _, _, _) if name != to.name =>
          doUpdate(tt.copy(name = to.name, code = encode(to.name), description = to.description)).flatMapConcat(res => updateTypeName(id, to.name).map(_ => res))
        case tt =>
          doUpdate(tt.copy(name = to.name, code = encode(to.name), description = to.description))
      }
      .orElse(single(Failure(RootException(INVALID_REFERENCE, "Entity not found"))))
  }

  def delete(id: String)(implicit logger: String = s"$Key#delete"): Future[Result[TransactionTypeTO]] = perform {
    countByType(id)
      .filter(_ == 0L)
      .flatMapConcat(_ => single(id).via(remove))
      .map(_ => Result())
      .orElse(single(Result(INCONSISTENCY_OPERATION, "Used types can not be deleted")))
  }

  def get(id: String)(implicit logger: String = s"$Key#get"): Future[Result[TransactionTypeTO]] = performTry {
    single(id)
      .via(load)
      .map(getTrialTransactionTypeTO)
      .orElse(single(Failure(RootException(INVALID_REFERENCE, "Entity not found"))))
  }

  def search(implicit context: Context, logger: String = s"$Key#search"): Future[Result[PagedList[TransactionTypeTO]]] = perform {
    single(DefaultCriteria(context))
      .via(loadAll)
      .fold(Seq[TransactionType]())(_ :+ _)
      .map(types => Result(PagedList(types.map(getTransactionTypeTO), types.size)))
      .orElse(single(Result(PagedList(Seq[TransactionTypeTO](), 0))))
  }

  //<editor-fold desc="Support Functions">

  private def doUpdate(tt: TransactionType): Source[Try[TransactionTypeTO], NotUsed] = single(tt).via(modify)

  //</editor-fold>
}
