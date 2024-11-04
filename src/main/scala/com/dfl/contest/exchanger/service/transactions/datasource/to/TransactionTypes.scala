package com.dfl.contest.exchanger.service.transactions.datasource.to

import akka.stream.alpakka.mongodb.DocumentUpdate
import com.dfl.contest.exchanger.service._
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionTypes.TransactionType
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.{combine, set}
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant.now

object TransactionTypes {

  case class TransactionTypeTO(id: Option[String], name: String, description: Option[String])

  //<editor-fold desc="Conversions">

  def getTransactionType(to: TransactionTypeTO): TransactionType = TransactionType(None, to.name, encode(to.name), to.description, now, now)

  def getTransactionTypeUpdate(tuple: (String, TransactionType)): DocumentUpdate = {
    val (id, tt) = tuple
    val update = tt match {
      case TransactionType(_, name, code, Some(description), _, _) => combine(set("name", name), combine(set("code", code), combine(set("description", description), set("updatedAt", now))))
      case TransactionType(_, name, code, None, _, _) => combine(set("name", name), combine(set("code", code), set("updatedAt", now)))
    }
    DocumentUpdate(
      filter = equal("_id", new ObjectId(id)),
      update = update
    )
  }

  //</editor-fold>

  //<editor-fold desc="SerDes">

  implicit val TransactionTypeTOFormat: RootJsonFormat[TransactionTypeTO] = jsonFormat3(TransactionTypeTO)

  //</editor-fold>
}
