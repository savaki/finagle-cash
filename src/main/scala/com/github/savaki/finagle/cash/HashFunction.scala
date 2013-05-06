package com.github.savaki.finagle.cash

import java.util
import scala.util.hashing.MurmurHash3
import scala.util.Random
import scala.collection.JavaConversions._

/**
 * @author matt
 */

trait HashFunction {
  def apply(obj: String): HashKey
}

class Murmur3HashFunction(seed: Int = Random.nextInt()) extends HashFunction with CashConstants {
  def apply(obj: String): HashKey = {
    val value: Int = MurmurHash3.arrayHash(obj.getBytes(DEFAULT_CHARSET))
    HashKey(value)
  }
}

case class HashKey(value: Int)

class ConsistentHash[T <: AnyRef](hashFunction: HashFunction, nodes: Seq[T], numberOfReplicas: Int = 128) {
  private[this] val circle: util.SortedMap[Int, T] = new util.TreeMap[Int, T]
  private[this] var btree: CircularBTree[T] = null

  nodes.foreach(node => add(node))

  /**
   * flattens the circle (a SortedMap[Int, T]) into a binary searchable array, btree (a BTree[T])
   */
  protected def flatten() {
    val array = circle.tailMap(Integer.MIN_VALUE).toArray
    btree = new CircularBTree[T](array)
  }

  def add(node: T): ConsistentHash[T] = {
    for (i <- 0 until numberOfReplicas) {
      val value: Int = hashNode(node, i)
      val previous: T = circle.put(value, node)
      if (previous == null) {
        //        println("adding node at => [%12s] %s" format (value, node))
      } else {
        println("collision on #add => %s" format node)
      }
    }
    flatten()
    this
  }

  def hashNode(node: T, i: Int): Int = {
    hashFunction(node.toString + i).value
  }

  def remove(node: T): ConsistentHash[T] = {
    for (i <- 0 until numberOfReplicas) {
      val value: Int = hashNode(node, i)
      circle.remove(value)
    }
    flatten()
    this
  }

  def get(key: String): T = {
    if (circle.isEmpty) {
      throw new UnsupportedOperationException("illegal call to #get with ConsistentHash with no nodes defined!  Please use #add to add at least one node")

    } else {
      /**
       * originally, this was implemented as circle#tailMap(hashKey).  this implementation improves on it in two major
       * ways: it's about twice as fast and doesn't generate additional objects for the garbage collector to deal with
       */
      val hashKey: Int = hashFunction(key).value
      val index = btree.search(hashKey)
      btree.value(index)
    }
  }
}
