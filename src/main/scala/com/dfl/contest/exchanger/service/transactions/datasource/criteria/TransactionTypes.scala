package com.dfl.contest.exchanger.service.transactions.datasource.criteria

object TransactionTypes {

  sealed trait TransactionTypeCriteria

  case class DynamicCriteria(search: String, page: Int, size: Int) extends TransactionTypeCriteria

  object DynamicCriteria {
    def apply() = new DynamicCriteria("", 0, 20)
  }

  case class DefaultCriteria(page: Int, size: Int) extends TransactionTypeCriteria

  object DefaultCriteria {
    def apply() = new DefaultCriteria(0, 20)
  }
}
