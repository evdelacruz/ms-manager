package com.dfl.contest.exchanger.service.transactions.datasource

import akka.actor.{Actor, ActorLogging, Props}
import com.dfl.contest.exchanger.service.transactions.datasource.domain.TransactionCodeCommands._
import com.dfl.seed.akka.base.DateProtocol
import com.dfl.seed.akka.base.DateProtocol.DefaultTimeZone

import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern

class TransactionCoder extends Actor with ActorLogging {
  import context._
  private val Pattern = ofPattern("yyMMddHH")

  override def receive: Receive = handle(Map())

  private def handle(suffixes: Map[String, Int]): Receive = {
    case CodeRequest(date) =>
      val key = date.atZone(DefaultTimeZone).format(Pattern)
      val suffix = suffixes.getOrElse(key, 1)
      become(handle(suffixes.updated(key, suffix + 1)))
      sender() ! s"T$key${suffix.toString.reverse.padTo(8, '0').reverse}"
    case UpdateSuffix(code) =>
      val key = code.substring(1, 9)
      val suffix = code.substring(9).replace("0", "").toInt
      become(handle(suffixes.updated(key, suffix + 1)))
      sender() ! true
  }
}

object TransactionCoder {
  def init():Props = Props(new TransactionCoder())
}
