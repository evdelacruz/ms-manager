package com.dfl.contest

package object exchanger {

  case class Authentication(userId: String, token: String)

  case class Context(authentication: Authentication, params: Seq[(String, String)])
}
