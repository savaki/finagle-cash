package com.github.savaki.finagle.cash.builder

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.savaki.finagle.cash.{Murmur3HashFunction, KeyExtractor}
import com.twitter.finagle.Service
import com.twitter.util.Future

/**
 * @author matt
 */

class CashServiceTest extends FlatSpec with ShouldMatchers {
  val hashFunction: Murmur3HashFunction = new Murmur3HashFunction()

  val echoService: Service[String, String] = new Service[String, String] {
    def apply(request: String): Future[String] = Future.value(request)
  }

  "#withKey" should "override key used" in {
    val detonator = new KeyExtractor[String] {
      def apply(request: String): String = throw new UnsupportedOperationException("should not be called if key was overridden")
    }
    val service = new CashService[String, String](detonator, hashFunction, Seq(echoService))
    val key = "theKey"
    val anotherKey = "anotherKey"
    val result: String = service.withKey(key) {
      service(anotherKey).get
    }
    result should be(anotherKey)
  }
}