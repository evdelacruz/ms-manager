package com.dfl.contest.exchanger.service.infrastructure.datasource.domain

import com.dfl.seed.akka.stream.mongodb.MongoDef.getCollectionConfiguration
import com.dfl.seed.akka.stream.mongodb.{CollectionConfiguration, DefaultDatabase}
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.bson.codecs.Macros.createCodecProvider
import org.mongodb.scala.bson.{Document, ObjectId}

import java.time.Instant

object Environments {

  private[service] val Codecs = fromProviders(classOf[MongoEnvironment])

  private[service] implicit val Collection: CollectionConfiguration[MongoEnvironment, Document] = getCollectionConfiguration("envs", classOf[MongoEnvironment], Codecs)

  private[service] case class MongoEnvironment(_id: ObjectId, name: String, createdAt: Instant, updatedAt: Instant)
}
