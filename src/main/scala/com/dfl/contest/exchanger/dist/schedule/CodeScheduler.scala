package com.dfl.contest.exchanger.dist.schedule

import akka.actor.Cancellable
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source.tick
import com.dfl.contest.exchanger.facade.TransactionFacade.refreshCode
import com.dfl.seed.akka.stream.base.Materializer

import java.time.Instant.now

object CodeScheduler {

  def schedule: Cancellable = {
    import scala.concurrent.duration._

    tick(1.seconds, 30.day, 0)
      .map(_ => now)
      .via(refreshCode)
      .to(Sink.ignore)
      .run()
  }
}
