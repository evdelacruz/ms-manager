package com.dfl.contest.exchanger.service.transactions.datasource.to

import akka.stream.alpakka.mongodb.DocumentUpdate
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes.TransactionType
import com.dfl.seed.akka.base.error.RootException
import com.dfl.seed.akka.base.error.ErrorCode.UNKNOWN_ERROR
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.{combine, set}
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant.now
import scala.util.{Failure, Success, Try}

object TransactionTypes {

  case class TransactionTypeCreateTO(name: String, description: Option[String])

  case class TransactionTypeUpdateTO(id: Option[String], name: String, description: Option[String])

  case class TransactionTypeTO(id: String, name: String, description: Option[String])

  //<editor-fold desc="Conversions">

  def getTransactionType(to: TransactionTypeCreateTO): TransactionType = TransactionType(None, to.name, encode(to.name), to.description, now, now)

  def getTransactionTypeUpdate(tt: TransactionType): DocumentUpdate = {
    val update = tt match {
      case TransactionType(_, name, code, Some(description), _, _) => combine(set("name", name), combine(set("code", code), combine(set("description", description), set("updatedAt", now))))
      case TransactionType(_, name, code, None, _, _) => combine(set("name", name), combine(set("code", code), set("updatedAt", now)))
    }
    DocumentUpdate(
      filter = equal("_id", tt._id.get),
      update = update
    )
  }

  def getTrialTransactionTypeTO(tt: TransactionType): Try[TransactionTypeTO] = tt._id match {
    case Some(id) => Success(TransactionTypeTO(id.toHexString, tt.name, tt.description))
    case None => Failure(RootException(UNKNOWN_ERROR, "No identifier were assigned to the transaction type"))
  }

  def getTransactionTypeTO(tt: TransactionType): TransactionTypeTO = TransactionTypeTO(tt._id.map(_.toHexString).orNull, tt.name, tt.description)

  //</editor-fold>

  //<editor-fold desc="SerDes">

  implicit val TransactionTypeTOFormatter: RootJsonFormat[TransactionTypeTO] = jsonFormat3(TransactionTypeTO)

  implicit val TransactionTypeCreateTOFormatter: RootJsonFormat[TransactionTypeCreateTO] = jsonFormat2(TransactionTypeCreateTO)

  implicit val TransactionTypeUpdateTOFormatter: RootJsonFormat[TransactionTypeUpdateTO] = jsonFormat3(TransactionTypeUpdateTO)

  //</editor-fold>
}
