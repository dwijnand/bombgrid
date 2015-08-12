package com.dwijnand.bombgrid
package tui

object Main {
  def main(args: Array[String]): Unit = {
    val seed = Rng.randomSeed()
    println(s"Using seed: $seed")
    val rng = Rng(seed)

    val size = (16, 16)
    val bombCount = 51

    val coords = for (y <- 0 until size._2; x <- 0 until size._1) yield (x, y)

    val (shuffledCoords, rng2) = rng.shuffle(coords)
    val bombsCoords = shuffledCoords.take(bombCount).toSet[(Int, Int)].contains _

    val bombNoBombs =
      0 until size._2 map { y =>
        0 until size._1 map { x =>
          x -> y -> (if (bombsCoords(x -> y)) IsBomb else NoBomb)
        }
      }

    val grid = {
      bombNoBombs map { row =>
        row map {
          case (xy, NoBomb) => DigitCell(sumBombs(bombNoBombs, xy, size))
          case (_, IsBomb)  => BombCell
        }
      }
    }

    grid foreach { row =>
      println(row.map { case BombCell => "*" ; case DigitCell(n) => "" + n }.mkString)
    }

    ()
  }

  def sumBombs(grid: IndexedSeq[IndexedSeq[((Int, Int), BombNoBomb)]], xy: (Int, Int), size: (Int, Int)): Int = {
    val ul = if (xy._2 > 0 && xy._1 > 0)           grid(xy._2 - 1)(xy._1 - 1)._2 else NoBomb
    val uc = if (xy._2 > 0)                        grid(xy._2 - 1)(xy._1)._2     else NoBomb
    val ur = if (xy._2 > 0 && xy._1 + 1 < size._1) grid(xy._2 - 1)(xy._1 + 1)._2 else NoBomb

    val cl = if (xy._1 > 0)           grid(xy._2)(xy._1 - 1)._2 else NoBomb
    val cr = if (xy._1 + 1 < size._1) grid(xy._2)(xy._1 + 1)._2 else NoBomb

    val dl = if (xy._2 + 1 < size._2 && xy._1 > 0)           grid(xy._2 + 1)(xy._1 - 1)._2 else NoBomb
    val dc = if (xy._2 + 1 < size._2)                        grid(xy._2 + 1)(xy._1)._2     else NoBomb
    val dr = if (xy._2 + 1 < size._2 && xy._1 + 1 < size._1) grid(xy._2 + 1)(xy._1 + 1)._2 else NoBomb

    Vector(ul, uc, ur, cl, cr, dl, dc, dr).collect { case IsBomb => 1 }.sum
  }
}

trait BombNoBomb
case object IsBomb extends BombNoBomb
case object NoBomb extends BombNoBomb

trait Cell
case object BombCell extends Cell
final case class DigitCell(n: Int) extends Cell
