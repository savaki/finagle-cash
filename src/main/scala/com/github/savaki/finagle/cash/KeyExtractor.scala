package com.github.savaki.finagle.cash

import com.twitter.finagle.redis.protocol.{Command => RedisCommand, KeyCommand}
import com.twitter.finagle.memcached.protocol.{Command => MemcachedCommand, _}
import org.jboss.netty.buffer.ChannelBuffer
import com.twitter.finagle.http.Request

/**
 * @author matt.ho@gmail.com
 */
trait KeyExtractor[IN] extends CashConstants {
  def apply(request: IN): String
}

class UnableToDetermineKeyException(gripe: String) extends RuntimeException(gripe)

class RedisKeyExtractor extends KeyExtractor[RedisCommand] {
  def apply(request: RedisCommand): String = {
    if (request.isInstanceOf[KeyCommand]) {
      request.asInstanceOf[KeyCommand].key.toString(DEFAULT_CHARSET)
    } else {
      throw new UnableToDetermineKeyException("unable to determine key for command, %s" format request)
    }
  }
}

class MemcachedKeyExtractor extends KeyExtractor[MemcachedCommand] {
  def headKey(keys: Seq[ChannelBuffer]): String = {
    require(keys.length == 1, "MemcachedKeyExtractor currently only supports extracting 1 key at a time.  Use multiple parallel requests if you want multiple keys.")
    keys.head.toString(DEFAULT_CHARSET)
  }

  def apply(request: MemcachedCommand): String = {
    request match {
      case storage: StorageCommand => headKey(storage.keys)
      case retrieve: RetrievalCommand => headKey(retrieve.keys)
      case arithmetic: ArithmeticCommand => headKey(arithmetic.keys)
      case delete: Delete => delete.key.toString(DEFAULT_CHARSET)
      case _ => throw new UnableToDetermineKeyException("MemcachedKeyExtractor doesn't currently support this command => %s" format request)
    }
  }
}

class HttpCookieKeyExtractor(val cookieName: String = "_fc") extends KeyExtractor[Request] {
  def apply(request: Request): String = {
    request.cookies.iterator.find(cookie => cookieName.equalsIgnoreCase(cookie.getName)).getOrElse {
      throw new UnableToDetermineKeyException("HttpCookieKeyExtractor can't find cookie with name, %s" format cookieName)
    }.getValue
  }
}
