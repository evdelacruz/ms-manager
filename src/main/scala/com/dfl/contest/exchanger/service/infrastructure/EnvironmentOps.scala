package com.dfl.contest.exchanger.service.infrastructure

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dfl.seed.akka.stream.base.Types.SafeSource.single
import com.dfl.seed.akka.stream.mongodb.MongoOps.findOne
import com.dfl.contest.exchanger.service.EnvironmentName
import com.dfl.contest.exchanger.service.infrastructure.datasource.domain.Environments._
import org.mongodb.scala.model.Filters.equal

object EnvironmentOps {
  private val Key = "service-infrastructure-environment"

  def loadMongoEnvironment(implicit logger: String = s"$Key#findmongoenvironment"): Source[Option[MongoEnvironment], NotUsed] =
    findOne(equal("name", EnvironmentName))
      .map(Some(_))
      .orElse(single(None))
}
