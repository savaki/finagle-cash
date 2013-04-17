package com.github.savaki.finagle.cash.builder

import com.twitter.finagle.Service
import com.twitter.util.Future
import com.github.savaki.finagle.cash.{KeyExtractor, HashKey, HashFunction}

class CashService[IN, OUT](keyExtractor:KeyExtractor[IN], hashFunction: HashFunction) extends Service[IN, OUT] {
  private[this] val keyToUse = new ThreadLocal[HashKey] {
    override def initialValue() = HashKey(0)
  }

  def apply(request: IN): Future[OUT] = {
    var key = keyToUse.get()
    if (key.value == 0) {
      key = hashFunction(keyExtractor(request))
    }

    val service = lookup(key)
    service(request)
  }

  def lookup(hashKey: HashKey): Service[IN, OUT] = {
    null
  }

  def hashKey(request: IN): HashKey = {
    hashFunction(keyExtractor(request))
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

