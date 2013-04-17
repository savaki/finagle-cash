package com.github.savaki.finagle.cash

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.DefaultCookie

/**
 * @author matt
 */

class HttpCookieKeyExtractorTest extends FlatSpec with ShouldMatchers {
  val extractor = new HttpCookieKeyExtractor

  "#apply" should "should extract finagle-cash cookie from http header" in {
    val request = Request("/")
    val key: String = "value"
    request.cookies += new DefaultCookie(extractor.cookieName, key)
    extractor(request) should be(key)
  }

  "#apply" should "raise exception in case where http cookie cannot be found" in {
    evaluating {
      extractor(Request("/"))
    } should produce[UnableToDetermineKeyException]
  }
}
