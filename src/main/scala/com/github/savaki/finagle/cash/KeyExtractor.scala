package com.github.savaki.finagle.cash

import com.twitter.finagle.redis.protocol.KeyCommand
import org.jboss.netty.buffer.ChannelBuffer

/**
 * @author matt.ho@gmail.com
 */
trait KeyExtractor[IN, T] {
  def apply(request: IN): T
}

class RedisKeyExtractor extends KeyExtractor[KeyCommand, ChannelBuffer] {
  def apply(request: KeyCommand): ChannelBuffer = {
    request.key
  }
}
