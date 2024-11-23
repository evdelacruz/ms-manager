package com.dfl.contest.exchanger.service.transactions

import akka.NotUsed
import akka.stream.alpakka.mongodb.DocumentUpdate
import akka.stream.scaladsl.{Flow, Source}
import com.dfl.contest.exchanger.service.transactions.datasource.TransactionCoder.{init => initializeCoder}
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.TransactionTypes.IdCriteria
import com.dfl.contest.exchanger.service.transactions.datasource.criteria.Transactions._
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionCodeCommands.{CodeRequest, UpdateSuffix}
import com.dfl.contest.exchanger.service.transactions.datasource.domain.Transactions.{Pipeline => getSummary, getSummary => getDinamicSummary, _}
import com.dfl.contest.exchanger.service.transactions.datasource.to.Transactions.{ConversionData, TransactionTO, getTransaction => getTransactionTO}
import com.dfl.seed.akka.base.{Timeout, getActor}
import com.dfl.seed.akka.stream.base.Types.SafeFlow
import com.dfl.seed.akka.stream.base.Types.SafeSource.{empty, single}
import com.dfl.seed.akka.stream.mongodb.MongoOps.{aggregate, count, findMany, findOne, insertOne, updateMany}
import com.dfl.seed.akka.stream.mongodb.bson.getDefaultSerializationSettings
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.{equal, exists}
import org.mongodb.scala.model.Sorts.{ascending, descending}
import org.mongodb.scala.model.Updates.set
import spray.json._

import java.time.Instant
import java.time.Instant.now
import scala.util.{Success, Try}

object TransactionOps {
  private val Key = "service-transactions-transactiontypeops"
  private val Coder = getActor("coder", initializeCoder())

  def add(implicit logger: String = s"$Key#add"): Flow[ConversionData, Try[TransactionTO], NotUsed] =
    SafeFlow[ConversionData]
      .flatMapConcat(getTransaction)
      .via(insertOne)
      .map(transaction => Success(getTransactionTO(transaction)))

  def setLastCode(implicit logger: String = s"$Key#find"): Flow[Instant, Boolean, NotUsed] = SafeFlow[Instant]
    .flatMapConcat(_ => findOne(filter = exists("_id"), sort = descending("createdAt")).map(transaction => UpdateSuffix(transaction.transactionCode)))
    .ask[Boolean](Coder)

  def updateTypeName(_id: ObjectId, name: String)(implicit logger: String = s"$Key#update-type-name"): Source[Long, NotUsed] =
    single(DocumentUpdate(equal("transactionType.id", _id), set("transactionType.name", name))).via(updateMany).map(_.getModifiedCount)

  def loadAll(implicit logger: String = s"$Key#find-all"): Flow[TransactionCriteria, TransactionTO, NotUsed] = SafeFlow[TransactionCriteria]
    .flatMapConcat {
      case DefaultCriteria(None, None, None, page, size) => findMany(filter = exists("_id"), sort = ascending("createdAt"), batchSize = size, skip = page * size, limit = size)
      case criteria@DefaultCriteria(_, _, _, page, size) => findMany(filter = criteria.getFilters, sort = ascending("createdAt"), batchSize = size, skip = page * size, limit = size)
      case _ => empty
    }
    .map(getTransactionTO)

  def lookup(criteria: TransactionCriteria)(implicit log: String = s"$Key#lookup"): Source[JsValue, NotUsed] = {
    val source = criteria match {
      case DefaultCriteria(None, None, None, _, _) => aggregate(getSummary)
      case criteria: DefaultCriteria => aggregate(getDinamicSummary(criteria.getFilters))
      case _ => empty
    }
    source
      .map(_.toJson(getDefaultSerializationSettings).parseJson)
  }

  def countByType(id: String)(implicit logger: String = s"$Key#count-by-type"): Source[Long, NotUsed] = IdCriteria(id) match {
    case IdCriteria(Some(id)) => count(equal("transactionType.id", id))
    case _ => single(0L)
  }

  //<editor-fold desc="Support Functions">

  private def getTransaction(data: ConversionData): Source[Transaction, NotUsed] = single(CodeRequest(data._3))
    .ask[String](Coder)
    .map(code => Transaction(None, data._2, code, data._1.fromCurrency, data._1.toCurrency, data._1.amount, data._1.amount * data._4, data._4, data._5, now, now))

  //</editor-fold>
}
