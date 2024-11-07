package com.dfl.contest.exchanger.service.transactions.datasource.domain

import com.dfl.seed.akka.base.JsonProtocol._
import com.dfl.seed.akka.stream.mongodb.JsonProtocol._
import com.dfl.seed.akka.stream.mongodb.MongoDef.getCollectionConfiguration
import com.dfl.seed.akka.stream.mongodb.{CollectionConfiguration, DefaultDatabase}
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.bson.codecs.Macros.{createCodecProviderIgnoreNone => ignoreNone}
import org.mongodb.scala.bson.{Document, ObjectId}
import org.mongodb.scala.result.InsertOneResult
import spray.json.DefaultJsonProtocol._
import spray.json._

import java.time.Instant

object TransactionTypes {
  private[transactions] val Codecs = fromProviders(ignoreNone(classOf[TransactionType]))

  private[transactions] implicit val Collection: CollectionConfiguration[TransactionType, Document] = getCollectionConfiguration("transactiontypes", classOf[TransactionType], Codecs)

  case class TransactionType(_id: Option[ObjectId], name: String, code: String, description: Option[String], createdAt: Instant, updatedAt: Instant)

  //<editor-fold desc="Functions">

  private[transactions] implicit def mapInsertion(operation: TransactionType, result: InsertOneResult): TransactionType =
    operation.copy(_id = Some(result.getInsertedId.asObjectId().getValue))

  //</editor-fold>

  //<editor-fold desc="SerDes">

  implicit val TransactionTypeFormat: RootJsonFormat[TransactionType] = jsonFormat6(TransactionType)

  //</editor-fold>
}
