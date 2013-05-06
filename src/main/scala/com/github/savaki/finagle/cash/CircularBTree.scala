package com.github.savaki.finagle.cash

/**
 * Provides a binary search algorithm that handles searches in support of a consistent hash.  Specifically,
 * CircularBTree performs the following:
 *
 * 1. if we have an exact key match, perform just like a standard BTree and return the index of the matching key
 * 2. if the search key > largest key or key < smallest key, return the index of the largest key
 * 3. if the search key is between two keys return the index of the smaller key
 *
 * @author matt.ho@gmail.com
 */
class CircularBTree[T <: AnyRef](circleArray: Array[(Int, T)]) {
  /**
   * holds the number of distinct nodes in this btree
   */
  val distinctNodes = circleArray.groupBy(_._2).size

  def value(index: Int): T = {
    circleArray(index)._2
  }

  /**
   * 1. if we have an exact key match, perform just like a standard BTree and return the index of the matching key
   * 2. if the search key > largest key or key < smallest key, return the index of the largest key
   * 3. if the search key is between two keys return the index of the smaller key
   *
   * @param key the key we're searching for
   * @param indexMin the lower bounding index
   * @param indexMax the upper bounding index
   */
  def search(key: Int, indexMin: Int = 0, indexMax: Int = circleArray.length - 1): Int = {
    if (indexMax < indexMin) {
      if (indexMax < 0) {
        // wrapping key ... smaller than smallest
        circleArray.length - 1

      } else if (indexMin >= circleArray.length) {
        // wrapping key ... larger than largest
        circleArray.length - 1

      } else {
        // between two keys
        indexMax
      }

    } else {
      val indexMidPoint: Int = (indexMax + indexMin) / 2
      val midpointKey = circleArray(indexMidPoint)._1

      if (midpointKey > key) {
        search(key, indexMin, indexMidPoint - 1)

      } else if (midpointKey < key) {
        search(key, indexMidPoint + 1, indexMax)

      } else {
        indexMidPoint
      }
    }
  }

  /**
   * return the N nodes that cover the specified key
   *
   * Logic:
   *
   * 1. using #search, find the index associated with the key provided
   * 2. going in an ascending fashion, return depth number of unique nodes in our circleArray
   *
   * @param key the search item
   * @param depth how many elements to return
   * @return depth number of elements
   */
  def nodes(key: Int, depth: Int = 1)(implicit t: Manifest[T]): Array[T] = {
    require(depth <= distinctNodes, "#nodes request called with depth that exceeded number of distinct nodes")

    val result = new Array[T](depth)

    var position = 0
    var index = search(key)

    while (position < depth) {
      val node: T = circleArray(index)._2
      if (result.contains(node) == false) {
        // ensure that values in the result are unique
        result(position) = node
        position = position + 1
      }
      index = (index + 1) % circleArray.length
    }

    result
  }
}
