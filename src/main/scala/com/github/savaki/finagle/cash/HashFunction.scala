package com.github.savaki.finagle.cash

import java.util
import scala.util.hashing.MurmurHash3
import scala.util.Random

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

class ConsistentHash[T <: AnyRef](hashFunction: HashFunction, nodes: Seq[T], numberOfReplicas: Int = 64) {
  private[this] val circle: util.SortedMap[Int, T] = new util.TreeMap[Int, T]

  nodes.foreach(node => add(node))

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
    this
  }

  def get(key: String): T = {
    if (circle.isEmpty) {
      throw new UnsupportedOperationException("illegal call to #get with ConsistentHash with no nodes defined!  Please use #add to add at least one node")

    } else {
      val value: Int = hashFunction(key).value
      val tailMap: util.SortedMap[Int, T] = circle.tailMap(value)
      var nodeKey: Int = 0
      if (tailMap.isEmpty) {
        // key has wrapped around the circle
        nodeKey = circle.firstKey()

      } else {
        // otherwise, we found in the key in the keyspace
        nodeKey = tailMap.firstKey()
      }
      circle.get(nodeKey)
    }
  }
}
