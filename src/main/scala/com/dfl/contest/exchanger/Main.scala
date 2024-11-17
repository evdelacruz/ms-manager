package com.dfl.contest.exchanger

import com.dfl.contest.exchanger.configuration.DefaultContext.getRunnable
import com.dfl.seed.akka.base.Application

/**
 * Main application class. Starting point of the application running.
 *
 * @author evdelacruz
 * @since 0.1.0
 */
object Main extends Application {
  val runnable = getRunnable
  runnable.run()
}
