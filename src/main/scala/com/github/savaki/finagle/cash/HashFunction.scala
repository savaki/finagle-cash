package com.github.savaki.finagle.cash

import com.github.savaki.finagle.cash.builder.HashKey

/**
 * @author matt
 */

trait HashFunction[T] {
  def apply(obj: T): HashKey
}