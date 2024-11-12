package com.dfl.contest.exchanger.configuration

import akka.actor.Cancellable
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.dfl.contest.exchanger.dist.rest.CurrencyRoute.{Routes => CurrencyRoutes}
import com.dfl.contest.exchanger.dist.rest.HealthcheckRoute.{Routes => HealthcheckRoutes}
import com.dfl.contest.exchanger.dist.rest.TransactionRoute.{Routes => TransactionRoutes}
import com.dfl.contest.exchanger.dist.rest.TransactionTypeRoute.{Routes => TransactionTypeRoutes}
import com.dfl.contest.exchanger.dist.schedule.CurrencyScheduler
import com.dfl.seed.akka.base.{GlobalConfig, Name, getActor}
import com.dfl.seed.akka.http.Routing.{wrap => path}
import com.dfl.seed.akka.http.startServer

/**
 * Configuration class for the default context.
 *
 * @author evdelacruz
 * @since 0.1.0
 */
object DefaultContext {
  private val Settings: CorsSettings = CorsSettings(GlobalConfig)

  private val Routes: Route = cors(Settings) {
    pathPrefix(Name) {
      path("api", HealthcheckRoutes, CurrencyRoutes, TransactionTypeRoutes, TransactionRoutes)
    }
  }

  private val Tasks = Seq(CurrencyScheduler.schedule)

  /**
   * Retrieves the application that manages all the pipeline orchestrations.
   */
  def getRunnable: Runnable = Runnable(Routes, Tasks)
}

/**
 * Class designed to run the pipeline application from the project app implementation.
 *
 * @author evdelacruz
 * @since 0.1.0
 */
class Runnable(routes: Route, tasks: Seq[Cancellable]) {

  /**
   * Launches all the routes running logic.
   *
   * @since 0.1.0
   */
  def run(): Unit = {
    startServer(routes)
  }
}

object Runnable {
  def apply(routes: Route, tasks: Seq[Cancellable]): Runnable = new Runnable(routes, tasks)
}