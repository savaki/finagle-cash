package com.github.savaki.finagle.cash

/**
 * @author matt
 */

trait HashFunction[T] {
  def hash(obj: T)
}