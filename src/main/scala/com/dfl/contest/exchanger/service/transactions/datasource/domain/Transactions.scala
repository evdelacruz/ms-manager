package com.dfl.contest.exchanger.service.transactions.datasource.domain

import com.dfl.contest.exchanger.service._
import com.dfl.seed.akka.stream.mongodb.{CollectionConfiguration, DefaultDatabase}
import com.dfl.seed.akka.stream.mongodb.MongoDef.getCollectionConfiguration
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.bson.{Document, ObjectId}
import org.mongodb.scala.bson.codecs.Macros.{createCodecProvider, createCodecProviderIgnoreNone => ignoreNone}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Aggregates.`match`
import org.mongodb.scala.result.InsertOneResult

import java.time.Instant

object Transactions {
  private[transactions] val Codecs = fromProviders(ignoreNone(classOf[Transaction]), classOf[TransactionType])

  private[transactions] implicit val Collection: CollectionConfiguration[Transaction, Document] = getCollectionConfiguration("transactions", classOf[Transaction], Codecs)

  case class TransactionType(id: ObjectId, name: String)

  case class Transaction(_id: Option[ObjectId], transactionType: TransactionType, transactionCode: String,
                         fromCurrency: String, toCurrency: String, amount: Double, amountConverted: Double,
                         exchangeRate: Double, userId: String, createdAt: Instant, updatedAt: Instant)

  //<editor-fold desc="Queries">

  private[transactions] def getSummary(filter: Bson): Seq[Bson] = `match`(filter) +: Pipeline

  private[transactions] val Pipeline = Seq[Bson](
    """
     {
       $facet: {
         types : [
           {
             $group: {
               _id: '$transactionType.id',
               name: { $first: '$transactionType.name' },
               count: { $sum: 1 },
               totalAmount: { $sum: '$amount' },
               aveAmount: { $avg: '$amount' },
             }
           },
           {
             $group: {
               _id: null,
               count: { $sum: '$count' },
               transactionsByType: { $addToSet: { k: '$name', v: '$count' } },
               totalAmountByTransactionType: { $addToSet: { k: '$name', v: '$totalAmount' } },
               averageAmountByTransactionType: { $addToSet: { k: '$name', v: '$aveAmount' } }
             }
           },
           {
             $project: {
               _id: 0,
               totalTransactions: '$count',
               transactionsByType: { $arrayToObject: '$transactionsByType' },
               totalAmountByTransactionType: { $arrayToObject: '$totalAmountByTransactionType' },
               averageAmountByTransactionType: { $arrayToObject: '$averageAmountByTransactionType' }
             }
           }
         ],
         currencies: [
           {
             $group: {
               _id: '$toCurrency',
               amount: { $sum: '$amountConverted' }
             }
           },
           {
             $group: {
               _id: null,
               totalAmountConvertedByCurrency: { $addToSet: { k: '$_id', v: '$amount' } }
             }
           },
           {
             $project: {
               _id: 0,
               totalAmountConvertedByCurrency: { $arrayToObject: '$totalAmountConvertedByCurrency' }
             }
           }
         ]
       }
     }
    """,
    """
     {
       $project: {
         totalTransactions: { $arrayElemAt: ['$types.totalTransactions', 0] },
         transactionsByType: { $arrayElemAt: ['$types.transactionsByType', 0] },
         totalAmountConvertedByCurrency: { $arrayElemAt: ['$currencies.totalAmountConvertedByCurrency', 0] },
         totalAmountByTransactionType: { $arrayElemAt: ['$types.totalAmountByTransactionType', 0] },
         averageAmountByTransactionType: { $arrayElemAt: ['$types.averageAmountByTransactionType', 0] }
       }
     }
    """
  )

  //</editor-fold>

  //<editor-fold desc="Functions">

  private[transactions] implicit def mapInsertion(operation: Transaction, result: InsertOneResult): Transaction =
    operation.copy(_id = Some(result.getInsertedId.asObjectId().getValue))

  //</editor-fold>
}
