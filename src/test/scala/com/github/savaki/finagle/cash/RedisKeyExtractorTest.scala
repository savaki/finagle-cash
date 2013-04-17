package com.github.savaki.finagle.cash

import org.scalatest.FlatSpec
import com.twitter.finagle.redis.protocol.{FlushDB, Get}
import com.twitter.finagle.redis.util.StringToChannelBuffer
import org.scalatest.matchers.ShouldMatchers

/**
 * @author matt
 */

class RedisKeyExtractorTest extends FlatSpec with ShouldMatchers {
  val extractor = new RedisKeyExtractor

  "#apply" should "extract redis key" in {
    val key: String = "hello world"
    val command = Get(StringToChannelBuffer(key))
    extractor(command) should be(key)
  }

  "#apply" should "raise exception in case of redis commands that have no key" in {
    evaluating {
      extractor(FlushDB)
    } should produce[UnableToDetermineKeyException]
  }
}