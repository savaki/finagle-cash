package com.github.savaki.finagle.cash.builder

import org.scalatest.FlatSpec
import com.twitter.finagle.http.Http
import com.github.savaki.finagle.cash.HashFunction
import com.twitter.finagle.CodecFactory
import org.jboss.netty.handler.codec.http.HttpRequest

/**
 * @author matt
 */

class ConsistentHashBuilderTest extends FlatSpec {
  "#build" should "throw exception if not fully spec'd" in {
    /**
     * if you remove either the codec or the HashFunction, the code won't compile
     */
    ConsistentHashBuilder()
      .codec(Http())
      .hash(null.asInstanceOf[HashFunction[HttpRequest]])
      .build()
  }
}
