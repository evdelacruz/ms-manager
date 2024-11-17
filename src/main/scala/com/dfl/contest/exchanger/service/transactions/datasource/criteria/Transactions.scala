package com.dfl.contest.exchanger.service.transactions.datasource.criteria

import com.dfl.contest.exchanger.Context
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal, gte, lte}

import java.time.Instant
import scala.util.Try

object Transactions {

  sealed trait TransactionCriteria

  case class DefaultCriteria(startDate: Option[Instant], endDate: Option[Instant], transactionType: Option[ObjectId], page: Int, size: Int) extends TransactionCriteria {

    def getFilters: Bson = {
      val filters = startDate.map(gte("createdAt", _)) ++ endDate.map(lte("createdAt", _)) ++ transactionType.map(equal("transactionType.id", _))
      if (1 == filters.size) filters.head else and(filters.toSeq: _*)
    }
  }

  object DefaultCriteria {

    def apply(context: Context): DefaultCriteria = {
      val startDate = context.params.find(_._1 == "startDate").flatMap(tuple => Try(Instant.parse(tuple._2)).toOption)
      val endDate = context.params.find(_._1 == "endDate").flatMap(tuple => Try(Instant.parse(tuple._2)).toOption)
      val transactionType = context.params.find(_._1 == "transactionType").flatMap(tuple => Try(new ObjectId(tuple._2)).toOption)
      val page = context.params.find(_._1 == "page").flatMap(tuple => Try(tuple._2.toInt).toOption).getOrElse(0)
      val size = context.params.find(_._1 == "size").flatMap(tuple => Try(tuple._2.toInt).toOption).getOrElse(20)
      DefaultCriteria(startDate, endDate, transactionType, page, size)
    }
  }
}
