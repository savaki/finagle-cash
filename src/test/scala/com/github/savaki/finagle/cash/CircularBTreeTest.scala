package com.github.savaki.finagle.cash

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author matt.ho@gmail.com
 */
class CircularBTreeTest extends FlatSpec with ShouldMatchers {
  val values = Array(10 -> "ten", 20 -> "twenty", 30 -> "thirty")
  val btree = new CircularBTree(values)

  "#search" should "should find exact values" in {
    btree.search(10) should be(0)
    btree.search(20) should be(1)
    btree.search(30) should be(2)
  }

  it should "return the max index when key > max key" in {
    btree.search(35) should be(2)
  }

  it should "return the max index when key < min key" in {
    btree.search(5) should be(2)
  }

  it should "return the lower bounded key when search key between keys" in {
    btree.search(15) should be(0)
    btree.search(25) should be(1)
  }

  "#nodes" should "return a distinct list of keys" in {
    btree.nodes(10, depth = 2) should be(Array("ten", "twenty"))
    btree.nodes(10, depth = 3) should be(Array("ten", "twenty", "thirty"))
  }

  it should "wrap around" in {
    btree.nodes(30, depth = 2) should be(Array("thirty", "ten"))
    btree.nodes(30, depth = 3) should be(Array("thirty", "ten", "twenty"))
  }

  it should "not return the same key twice" in {
    val values = Array(10 -> "a", 20 -> "a", 30 -> "b", 40 -> "a", 50 -> "c")
    val btree = new CircularBTree(values)
    btree.nodes(10, depth = 2) should be(Array("a", "b"))
  }

  it should "throw an exception if the depth exceeds the number of distinct nodes" in {
    evaluating {
      btree.nodes(30, depth = 4)
    } should produce[RuntimeException]
  }
}
