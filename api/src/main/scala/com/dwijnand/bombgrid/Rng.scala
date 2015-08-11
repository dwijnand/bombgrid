package com.dwijnand.bombgrid

import scala.annotation.tailrec
import scala.collection.generic.{ CanBuildFrom => CBF }
import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicLong

final case class Rng(seed: Long) extends AnyVal {
  import Rng._

  def nextBits(bits: Int): (Int, Rng) = {
    val newSeed = (seed * multiplier + increment) & 0xFFFFFFFFFFFFL
    (newSeed >>> (48 - bits)).toInt -> Rng(newSeed)
  }

  def nextInt: (Int, Rng) = nextBits(32)

  def nextInt(bound: Int): (Int, Rng) = {
    val b = nextBits(31)
    val n = b._1
    val rng = b._2
    val m = bound - 1
    if ((bound & m) == 0)  // i.e., bound is a power of 2
      ((bound * n.toLong) >> 31).toInt -> rng
    else {
      @tailrec def go(s: (Int, Rng)): (Int, Rng) = {
        val n = s._1
        val rng = s._2
        val r = n % bound
        if (n - r + m < 0) go(rng nextBits 31)
        else r -> rng
      }
      go(n -> rng)
    }
  }

  def chooseInt(lowerBound: Int, upperBound: Int): (Int, Rng) = {
    val c = if (lowerBound < upperBound) lowerBound -> upperBound else upperBound -> lowerBound
    val l = c._1
    val h = c._2

    val diff = h - l
    if (diff == 0)
      (l, this)
    else {
      val n = nextInt
      val x = n._1
      val rng = n._2
      (l + (x.abs % (diff + 1)), rng)
    }
  }

  def choose[A](xs0: Traversable[A]): (A, Rng) = {
    val xs = xs0.toIndexedSeq
    val c = chooseInt(0, xs.size - 1)
    (xs(c._1), c._2)
  }

  def shuffle[T, CC[X] <: TraversableOnce[X]](xs: CC[T])(implicit cbf: CBF[CC[T], T, CC[T]]): (CC[T], Rng) = {
    val buf = new ArrayBuffer[T] ++= xs

    def swap(i1: Int, i2: Int): Unit = {
      val tmp = buf(i1)
      buf(i1) = buf(i2)
      buf(i2) = tmp
    }

    val rng = (buf.length to 2 by -1).foldLeft(this) { (rng, n) =>
      val i = rng nextInt n
      val k = i._1
      val rng2 = i._2
      swap(n - 1, k)
      rng2
    }

    (cbf(xs) ++= buf).result() -> rng
  }
}
object Rng {
  private val multiplier = 0x5DEECE66DL
  private val increment = 0xBL

  private val seedUniquifier = new AtomicLong(8682522807148012L)

  @tailrec private def newSeedUniquifier(): Long = {
    val current = seedUniquifier.get()
    val next = current * 181783497276652981L
    if (seedUniquifier.compareAndSet(current, next))
      next
    else newSeedUniquifier()
  }

  def randomSeed() = newSeedUniquifier() ^ System.nanoTime()
}
