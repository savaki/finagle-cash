package com.github.savaki.finagle.cash.builder

import com.twitter.finagle.Service
import com.twitter.util.Future
import com.github.savaki.finagle.cash.{ConsistentHash, KeyExtractor, HashFunction}

class CashService[IN, OUT](keyExtractor: KeyExtractor[IN], hashFunction: HashFunction, nodes: Seq[Service[IN, OUT]]) extends Service[IN, OUT] {
  private[this] val cash = new ConsistentHash[Service[IN, OUT]](hashFunction, nodes)

  private[this] val keyToUse = new ThreadLocal[String] {
    override def initialValue() = null
  }

  def apply(request: IN): Future[OUT] = {
    val service = lookup(request)
    service(request)
  }

  def lookup(request: IN): Service[IN, OUT] = {
    var key = keyToUse.get()
    if (key == null) {
      key = keyExtractor(request)
    }

    cash.get(key)
  }

  def withKey[T](key: String)(function: => T): T = {
    val original: String = keyToUse.get()
    keyToUse.set(key)
    try {
      function
    } finally {
      keyToUse.set(original)
    }
  }
}

