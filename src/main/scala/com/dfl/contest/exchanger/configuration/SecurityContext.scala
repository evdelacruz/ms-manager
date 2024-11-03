package com.dfl.contest.exchanger.configuration

import akka.http.scaladsl.server.Directives.{extractRequest, parameterSeq, authenticateOAuth2 => secure}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}
import com.auth0.jwt.JWT.require
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.auth0.jwt.interfaces.DecodedJWT
import com.dfl.contest.exchanger._
import com.dfl.contest.exchanger.service.{EnvironmentName => Realm}
import com.dfl.seed.akka.base.GlobalConfig

import java.time.Instant.now
import scala.util.Try

/**
 * Configuration class for the default context.
 *
 * @author evdelacruz
 * @since 0.1.0
 */
object SecurityContext {
  private val Verifier = require(HMAC256(GlobalConfig.getString("application.security.secret"))).build()

  def authenticate(credentials: Credentials): Option[Authentication] = credentials match {
    case Missing => None
    case Provided(token) => Try(Verifier.verify(token))
      .filter(jwt => null != jwt.getExpiresAt && now.isBefore(jwt.getExpiresAtAsInstant))
      .flatMap(getAuthentication)
      .toOption
  }

  def routesc(handle: Context => Route): Route = secure(Realm, authenticate) { authentication =>
    extractRequest { req =>
      parameterSeq { params =>
        handle(Context(authentication, params ++: req.headers.map(header => (header.name(), header.value())), now))
      }
    }
  }

  //<editor-fold desc="Support Functions">

  private def getAuthentication(token: DecodedJWT): Try[Authentication] = Try {
    val userId = token.getClaim("id").asString()
    Authentication(userId, token.getToken)
  }

  //</editor-fold>
}