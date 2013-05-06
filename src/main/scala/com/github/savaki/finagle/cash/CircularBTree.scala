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
        indexMin
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
}
