package com.dfl.contest

import java.time.Instant

package object exchanger {

  case class Authentication(userId: String, token: String)

  case class Context(authentication: Authentication, params: Seq[(String, String)], date: Instant)
}
