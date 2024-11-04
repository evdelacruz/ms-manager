package com.dfl.contest.exchanger.service.transactions

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.TransactionTypes.{DefaultCriteria, DynamicCriteria, TransactionTypeCriteria}
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes._
import com.dfl.contest.exchanger.service.transactions.datasource.to.TransactionTypes._
import com.dfl.seed.akka.base.error.ErrorCode._
import com.dfl.seed.akka.stream.base.Types.SafeFlow
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.base.facade.PagedList
import com.dfl.seed.akka.stream.mongodb.MongoOps._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{and, equal, exists, ne => notEqual}
import org.mongodb.scala.model.Sorts.ascending

import scala.util.{Failure, Success, Try}

/**
 * TODO Prevent used types deletion
 * TODO Add support for text search
 */
object TransactionTypeOps {
  private val Key = "service-wallet-transactiontypeops"

  def add(implicit logger: String = s"$Key#add"): Flow[TransactionTypeTO, Try[TransactionType], NotUsed] = SafeFlow[TransactionTypeTO]
    .flatMapConcat(validate)
    .flatMapConcat(mapTrial(single(_).via(insertOne).map(Success(_)).recover(Failure(_))))

  def update(implicit logger: String = s"$Key#update"): Flow[(String, TransactionTypeTO), Try[TransactionTypeTO], NotUsed] = SafeFlow[(String, TransactionTypeTO)]
    .flatMapConcat(tuple => validate(tuple._2).flatMapConcat(mapTrial(single(tuple._1, _).map(getTransactionTypeUpdate).via(updateOne).map(_ => Success(tuple._2)))))

  def remove(implicit logger: String = s"$Key#remove"): Flow[String, Boolean, NotUsed] = SafeFlow[String]
    .map(id => equal("_id", new ObjectId(id)))
    .via(deleteOne)
    .map(_.getDeletedCount != 0)

  def find(implicit logger: String = s"$Key#find"): Flow[String, Try[TransactionType], NotUsed]  = SafeFlow[String]
    .flatMapConcat(id => findOne(equal("_id", new ObjectId(id))))
    .map(Success(_))
    .orElse(single(Failure(ValidationException(INVALID_REFERENCE, "Entity not found"))))

  def findAll(implicit logger: String = s"$Key#find-all"): Flow[TransactionTypeCriteria, PagedList[TransactionType], NotUsed] = SafeFlow[TransactionTypeCriteria]
    .flatMapConcat {
      case DynamicCriteria(_, page, size) => findMany(filter = exists("_id"), sort = ascending("createdAt"), batchSize = size, skip = page + size)
      case DefaultCriteria(page, size) => findMany(filter = exists("_id"), sort = ascending("createdAt"), batchSize = size, skip = page + size)
    }
    .fold(Seq[TransactionType]())(_ :+ _)
    .map(PagedList(_))

  //<editor-fold desc="Support Functions">

  private def validate(to: TransactionTypeTO): Source[Try[TransactionType], NotUsed] = {
    implicit val logger: String = s"$Key#validate"
    single(getTransactionType(to))
      .flatMapConcat {
        case entity@TransactionType(None, name, code, _, _, _) => count(equal("code", code)).map(count => if (count == 0) Success(entity) else Failure(ValidationException(DUPLICATE_TRANSACTION_TYPE, s"The transaction type's  name '$name' is invalid. Please provide a valid name.")))
        case entity@TransactionType(Some(id), name, code, _, _, _) => count(and(notEqual("_id", id), equal("code", code))).map(count => if (count == 0) Success(entity) else Failure(ValidationException(DUPLICATE_TRANSACTION_TYPE, s"The transaction type's  name '$name' is invalid. Please provide a valid name.")))
      }
  }

  //</editor-fold>
}
