package com.dfl.contest.exchanger.service.transactions.datasource.criteria

import com.dfl.contest.exchanger.Context
import org.mongodb.scala.bson.ObjectId

object TransactionTypes {

  sealed trait TransactionTypeCriteria

  case class DynamicCriteria(search: String, page: Int, size: Int) extends TransactionTypeCriteria

  object DynamicCriteria {
    def apply() = new DynamicCriteria("", 0, 20)
  }

  case class DefaultCriteria(page: Int, size: Int) extends TransactionTypeCriteria

  object DefaultCriteria {

    def apply(): DefaultCriteria = DefaultCriteria(0, 20)

    def apply(context: Context): DefaultCriteria = {
      val page = context.params.find(_._1 == "page").map(_._2.toInt).getOrElse(0)
      val size = context.params.find(_._1 == "size").map(_._2.toInt).getOrElse(20)
      DefaultCriteria(page, size)
    }
  }

  case class IdCriteria(id: Option[ObjectId])

  object IdCriteria {

    def apply(id: String): IdCriteria = IdCriteria("[0-9a-fA-F]{24}$".r.findFirstIn(id).map(new ObjectId(_)))
  }
}
