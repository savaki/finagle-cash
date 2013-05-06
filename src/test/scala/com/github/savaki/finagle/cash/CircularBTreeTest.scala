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
    btree.search(15) should be(1)
    btree.search(25) should be(2)
  }
}
