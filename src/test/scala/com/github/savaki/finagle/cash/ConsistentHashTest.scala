package com.github.savaki.finagle.cash

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.util
import sun.misc.BASE64Encoder
import scala.collection.JavaConversions._

/**
 * @author matt
 */

class ConsistentHashTest extends FlatSpec with ShouldMatchers {
  val encoder = new BASE64Encoder

  val hashFunction: HashFunction = new Murmur3HashFunction

  "ConsistentHash" should "evenly distribute requests as nodes are added and removed" in {
    val nodes: Seq[String] = Seq("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N")
    val cash = new ConsistentHash[String](hashFunction, Seq())

    for (index <- 0 until nodes.length) {
      val keys = nodes.filter(node => node.head.toInt <= "A".head.toInt + index)
      cash.add(nodes(index))
      val results: util.HashMap[String, Int] = evaluate(keys, cash)
      verifyReasonablyBalanced(results)
    }

    for (index <- 0 until (nodes.length - 1)) {
      val removeIndex = nodes.length - (index + 1)
      val keys = nodes.filter(node => node.head.toInt < "A".head.toInt + removeIndex)
      cash.remove(nodes(removeIndex))
      val results: util.HashMap[String, Int] = evaluate(keys, cash)
      verifyReasonablyBalanced(results)
    }
  }

  it should "be performant when returning nodes" in {
    val nodes: Seq[String] = Seq("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N")
    val cash = new ConsistentHash[String](hashFunction, nodes)
    val started = System.currentTimeMillis()
    val count: Int = 100000
    for (i <- 1 to count) {
      cash.nodes(i.toString)
    }
    val elapsed = System.currentTimeMillis() - started
    println(s"${count} executions completed in ${elapsed}ms (${elapsed.toDouble / count.toDouble}ms)")
    elapsed should be < 1000L
  }

  /**
   * verifies that the keys are more or less evenly distributed in the buckets
   *
   * @param results the number of keys[Int] owned by host[String]
   */
  def verifyReasonablyBalanced(results: util.HashMap[String, Int]) {
    val totalKeys = results.values().sum
    val proportion = 1.toDouble / results.size().toDouble
    val delta = List(proportion * .2, .1).max
    val min = proportion - delta
    val max = proportion + delta

    results.values().foreach {
      keys => {
        val value = keys.toDouble / totalKeys
        value should be >= min
        value should be <= max
      }
    }
  }

  /**
   * for a random sampling of keys, determine which host would own the keyspace for those keys
   *
   * @param nodes the nodes registered with the consistent hash
   * @param cash the consistent hash instance to test
   * @return the distribution of queries by node
   */
  def evaluate(nodes: Seq[String], cash: ConsistentHash[String], count: Int = 100000): util.HashMap[String, Int] = {
    val counter = new util.HashMap[String, Int]
    nodes.foreach(node => counter.put(node, 0))
    val start = System.currentTimeMillis()
    for (i <- 1 to count) {
      val key: String = cash.get(i.toString)
      val value: Int = counter.get(key)
      counter.put(key, value + 1)
    }
    val elapsed = System.currentTimeMillis() - start
    println(s"${counter} => ${elapsed}ms")
    counter
  }
}
