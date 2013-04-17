package com.github.savaki.finagle.cash

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.twitter.finagle.memcached.protocol.{Quit, Get}
import com.twitter.finagle.redis.util.StringToChannelBuffer

/**
 * @author matt
 */

class MemcachedKeyExtractorTest extends FlatSpec with ShouldMatchers {
  val extractor = new MemcachedKeyExtractor

  "#apply" should "extract memcached key" in {
    val key: String = "hello"
    val command = Get(Seq(StringToChannelBuffer(key)))
    extractor(command) should be(key)
  }

  "#apply" should "raise exception in case of memcached commands that have no key" in {
    evaluating {
      extractor(Quit())
    } should produce[UnableToDetermineKeyException]
  }

}
