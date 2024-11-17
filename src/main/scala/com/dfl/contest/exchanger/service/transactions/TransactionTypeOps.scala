package com.dfl.contest.exchanger.service.transactions

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.TransactionTypes._
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes._
import com.dfl.contest.exchanger.service.transactions.datasource.to.TransactionTypes._
import com.dfl.seed.akka.base.error.RootException
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeFlow
import com.dfl.seed.akka.stream.base.Types.SafeSource.{empty, single}
import com.dfl.seed.akka.stream.mongodb.MongoOps._
import org.mongodb.scala.model.Filters.{and, equal, exists, ne => notEqual}
import org.mongodb.scala.model.Sorts.ascending

import scala.util.{Failure, Success, Try}

/**
 * TODO Prevent used types deletion
 * TODO Add support for text search
 */
object TransactionTypeOps {
  private val Key = "service-transactions-transactiontypeops"

  def add(implicit logger: String = s"$Key#add"): Flow[TransactionTypeCreateTO, Try[TransactionTypeTO], NotUsed] = SafeFlow[TransactionTypeCreateTO]
    .flatMapConcat(validate)
    .flatMapConcat(trial(single(_).via(insertOne).map(getTrialTransactionTypeTO)))

  def update(implicit logger: String = s"$Key#update"): Flow[TransactionType, Try[TransactionTypeTO], NotUsed] = SafeFlow[TransactionType]
    .flatMapConcat(tt => validate(tt).flatMapConcat(trial(single(_).map(getTransactionTypeUpdate).via(updateOne).map(_ => getTrialTransactionTypeTO(tt)))))

  def remove(implicit logger: String = s"$Key#remove"): Flow[String, Boolean, NotUsed] = getIdCriteria
    .flatMapConcat {
      case IdCriteria(Some(id)) => single(equal("_id", id)).via(deleteOne)
      case _ => empty
    }
    .map(_.getDeletedCount != 0)
    .orElse(single(true))

  def load(implicit logger: String = s"$Key#find"): Flow[String, TransactionType, NotUsed]  = getIdCriteria
    .flatMapConcat {
      case IdCriteria(Some(id)) => findOne(equal("_id", id))
      case _ => empty
    }

  def loadAll(implicit logger: String = s"$Key#find-all"): Flow[TransactionTypeCriteria, TransactionType, NotUsed] = SafeFlow[TransactionTypeCriteria]
    .flatMapConcat {
      case DynamicCriteria(_, page, size) => findMany(filter = exists("_id"), sort = ascending("createdAt"), batchSize = size, skip = page + size, limit = size)
      case DefaultCriteria(page, size) => findMany(filter = exists("_id"), sort = ascending("createdAt"), batchSize = size, skip = page * size, limit = size)
    }

  //<editor-fold desc="Support Functions">

  private def getIdCriteria:Flow[String, IdCriteria, NotUsed] = SafeFlow[String].map(IdCriteria(_))

  private def validate(tt: TransactionType): Source[Try[TransactionType], NotUsed] = {
    implicit val logger: String = s"$Key#validate"
    tt match {
      case entity@TransactionType(None, name, code, _, _, _) => count(equal("code", code)).map(count => if (count == 0) Success(entity) else Failure(RootException(DUPLICATE_TRANSACTION_TYPE, s"The transaction type's  name '$name' is invalid. Please provide a valid name.")))
      case entity@TransactionType(Some(id), name, code, _, _, _) => count(and(notEqual("_id", id), equal("code", code))).map(count => if (count == 0) Success(entity) else Failure(RootException(DUPLICATE_TRANSACTION_TYPE, s"The transaction type's  name '$name' is invalid. Please provide a valid name.")))
    }
  }

  private def validate(to: TransactionTypeCreateTO): Source[Try[TransactionType], NotUsed] = validate(getTransactionType(to))

  //</editor-fold>
}
