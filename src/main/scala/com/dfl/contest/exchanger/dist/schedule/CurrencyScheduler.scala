package com.dfl.contest.exchanger.dist.schedule

import akka.actor.Cancellable
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source.tick
import com.dfl.contest.exchanger.facade.CurrenciesFacade.refreshCurrencies
import com.dfl.seed.akka.stream.base.Materializer

import java.time.Instant.now

object CurrencyScheduler {

  def schedule: Cancellable = {
    import scala.concurrent.duration._

    tick(1.seconds, 30.minute, 0)
      .map(_ => now)
      .via(refreshCurrencies)
      .to(Sink.ignore)
      .run()
  }
}
