package com.github.savaki.finagle.cash.builder

import com.twitter.finagle.Service
import com.twitter.util.Future
import com.github.savaki.finagle.cash.HashFunction

class CashService[IN, OUT](hashFunction: HashFunction[IN]) extends Service[IN, OUT] {
  private[this] val keyToUse = new ThreadLocal[HashKey] {
    override def initialValue() = HashKey(0)
  }

  def apply(request: IN): Future[OUT] = {
    var key = keyToUse.get()
    if (key.value == 0) {
      key = hashFunction(request)
    }

    val service = lookup(key)
    service(request)
  }

  def lookup(hashKey: HashKey): Service[IN, OUT] = {
    null
  }

  def hashKey(request: IN): HashKey = {
    hashFunction(request)
  }

  def withHashKey[T](key: HashKey)(function: => T): T = {
    val original: HashKey = keyToUse.get()
    keyToUse.set(key)
    try {
      function
    } finally {
      keyToUse.set(original)
    }
  }
}

case class HashKey(value: Long)