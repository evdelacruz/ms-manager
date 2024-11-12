package com.dfl.contest.exchanger.service.transactions.datasource.domain

import com.dfl.seed.akka.stream.mongodb.{CollectionConfiguration, DefaultDatabase}
import com.dfl.seed.akka.stream.mongodb.MongoDef.getCollectionConfiguration
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.bson.{Document, ObjectId}
import org.mongodb.scala.bson.codecs.Macros.{createCodecProvider, createCodecProviderIgnoreNone => ignoreNone}

import java.time.Instant

object Transactions {
  private[transactions] val Codecs = fromProviders(ignoreNone(classOf[Transaction]), classOf[TransactionType])

  private[transactions] implicit val Collection: CollectionConfiguration[Transaction, Document] = getCollectionConfiguration("transactions", classOf[Transaction], Codecs)

  case class TransactionType(id: ObjectId, name: String)

  case class Transaction(_id: Option[ObjectId], transactionType: TransactionType, transactionCode: String,
                         fromCurrency: String, toCurrency: String, amount: BigDecimal, amountConverted: BigDecimal,
                         exchangeRate: Double, userId: String, createdAt: Instant, updatedAt: Instant)
}
