package com.dwijnand.bombgrid
package tui

import scala.io.AnsiColor._

object Main {
  def main(args: Array[String]): Unit = {
    val seed = Rng.randomSeed()
    println(s"Using seed: $seed")
    val rng = Rng(seed)

    val size = (16, 16)
    val bombCount = 51

    val (bombsCoords, rng2) = {
      val coords = for (y <- 0 until size._2; x <- 0 until size._1) yield (x, y)
      val (shuffledCoords, rng2) = rng.shuffle(coords)
      (shuffledCoords.take(bombCount).toSet[(Int, Int)].contains _, rng2)
    }

    sealed trait BombNoBomb
    case object IsBomb extends BombNoBomb
    case object NoBomb extends BombNoBomb

    val bombNoBombs =
      0 until size._2 map { y =>
        0 until size._1 map { x =>
          if (bombsCoords((x, y))) IsBomb else NoBomb
        }
      }

    def sumBombs(grid: IndexedSeq[IndexedSeq[BombNoBomb]], xy: (Int, Int)): Int = {
      val (x, y) = xy
      val (size_x, size_y) = size

      val ul = if (y > 0 && x > 0)          grid(y - 1)(x - 1) else NoBomb
      val uc = if (y > 0)                   grid(y - 1)(x)     else NoBomb
      val ur = if (y > 0 && x + 1 < size_x) grid(y - 1)(x + 1) else NoBomb

      val cl = if (x > 0)          grid(y)(x - 1) else NoBomb
      val cr = if (x + 1 < size_x) grid(y)(x + 1) else NoBomb

      val dl = if (y + 1 < size_y && x > 0)          grid(y + 1)(x - 1) else NoBomb
      val dc = if (y + 1 < size_y)                   grid(y + 1)(x)     else NoBomb
      val dr = if (y + 1 < size_y && x + 1 < size_x) grid(y + 1)(x + 1) else NoBomb

      Vector(ul, uc, ur, cl, cr, dl, dc, dr).collect { case IsBomb => 1 }.sum
    }

    val grid = {
      bombNoBombs.indices map { y =>
        val row = bombNoBombs(y)
        row.indices map { x =>
          row(x) match {
            case NoBomb => DigitCell(sumBombs(bombNoBombs, (x, y)))
            case IsBomb => BombCell
          }
        }
      }
    }

    grid foreach { row =>
      println(row.map {
        case BombCell     => BLACK + BOLD + "*" + RESET
        case DigitCell(0) => " "
        case DigitCell(1) => BLUE + BOLD + "1" + RESET
        case DigitCell(2) => GREEN + "2" + RESET
        case DigitCell(3) => RED + BOLD + "3" + RESET
        case DigitCell(4) => BLUE + "4" + RESET
        case DigitCell(5) => RED + "5" + RESET
        case DigitCell(6) => CYAN + "6" + RESET
        case DigitCell(7) => MAGENTA + "7" + RESET
        case DigitCell(8) => BOLD + "8" + RESET
      }.mkString)
    }

    ()
  }
}

sealed trait Cell
case object BombCell extends Cell
final case class DigitCell(n: Int) extends Cell
