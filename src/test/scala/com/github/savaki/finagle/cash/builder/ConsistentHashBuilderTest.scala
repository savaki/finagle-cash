package com.github.savaki.finagle.cash.builder

import org.scalatest.FlatSpec
import com.twitter.finagle.http.Http
import com.github.savaki.finagle.cash.HashFunction
import com.twitter.finagle.CodecFactory

/**
 * @author matt
 */

class ConsistentHashBuilderTest extends FlatSpec {
  "#build" should "throw exception if not fully spec'd" in {
    foo(Http.get())
    ConsistentHashBuilder()
      .codec(Http.get())
      .hash(null.asInstanceOf[HashFunction[String]])
//      .build()
  }

  def foo[IN1, OUT1](codec: CodecFactory[IN1, OUT1])(implicit in:Manifest[IN1]) {
    println(in.erasure.getCanonicalName)
  }
}
