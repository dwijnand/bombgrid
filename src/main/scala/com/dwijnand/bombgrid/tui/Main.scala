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

    val bombNoBombs =
      0 until size._2 map { y =>
        0 until size._1 map { x =>
          if (bombsCoords((x, y))) IsBomb else NoBomb
        }
      }

    val bombCounts: IndexedSeq[IndexedSeq[Option[BombCount]]] = BombCount fromGrid bombNoBombs

    val grid =
      bombCounts.map { row =>
        row.map { bc =>
          val cell = bc.map(DigitCell).getOrElse(BombCell)
          Block(cell, revealed = true)
        }
      }

    grid foreach { row =>
      println(row.map { b =>
        if (b.revealed) b.cell match {
          case BombCell                => BLACK + BOLD + "*" + RESET
          case DigitCell(BombCount._0) => " "
          case DigitCell(BombCount._1) => BLUE + BOLD + "1" + RESET
          case DigitCell(BombCount._2) => GREEN + "2" + RESET
          case DigitCell(BombCount._3) => RED + BOLD + "3" + RESET
          case DigitCell(BombCount._4) => BLUE + "4" + RESET
          case DigitCell(BombCount._5) => RED + "5" + RESET
          case DigitCell(BombCount._6) => CYAN + "6" + RESET
          case DigitCell(BombCount._7) => MAGENTA + "7" + RESET
          case DigitCell(BombCount._8) => BOLD + "8" + RESET
        } else "#"
      }.mkString)
    }

    ()
  }
}

sealed trait BombNoBomb
case object IsBomb extends BombNoBomb
case object NoBomb extends BombNoBomb

sealed trait BombCount extends Any { def value: Int }
object BombCount {
  case object _0 extends BombCount { val value = 0 }
  case object _1 extends BombCount { val value = 1 }
  case object _2 extends BombCount { val value = 2 }
  case object _3 extends BombCount { val value = 3 }
  case object _4 extends BombCount { val value = 4 }
  case object _5 extends BombCount { val value = 5 }
  case object _6 extends BombCount { val value = 6 }
  case object _7 extends BombCount { val value = 7 }
  case object _8 extends BombCount { val value = 8 }

  def fromGrid(grid: IndexedSeq[IndexedSeq[BombNoBomb]]): IndexedSeq[IndexedSeq[Option[BombCount]]] = {
    val size_y = grid.size
    val size_x = grid.head.size // TODO: Grid type

    def sumBombs(grid: IndexedSeq[IndexedSeq[BombNoBomb]], xy: (Int, Int)): BombCount = {
      val (x, y) = xy

      val ul = if (y > 0 && x > 0)          grid(y - 1)(x - 1) else NoBomb
      val uc = if (y > 0)                   grid(y - 1)(x)     else NoBomb
      val ur = if (y > 0 && x + 1 < size_x) grid(y - 1)(x + 1) else NoBomb

      val cl = if (x > 0)          grid(y)(x - 1) else NoBomb
      val cr = if (x + 1 < size_x) grid(y)(x + 1) else NoBomb

      val dl = if (y + 1 < size_y && x > 0)          grid(y + 1)(x - 1) else NoBomb
      val dc = if (y + 1 < size_y)                   grid(y + 1)(x)     else NoBomb
      val dr = if (y + 1 < size_y && x + 1 < size_x) grid(y + 1)(x + 1) else NoBomb

      BombCount(Vector(ul, uc, ur, cl, cr, dl, dc, dr).count(_ == IsBomb))
    }

    grid.indices map { y =>
      val row = grid(y)
      row.indices map { x =>
        row(x) match {
          case NoBomb => Some(sumBombs(grid, (x, y)))
          case IsBomb => None
        }
      }
    }
  }

  def apply(n: Int) =
    n match {
      case 0 => _0 ; case 1 => _1 ; case 2 => _2 ; case 3 => _3
      case 4 => _4 ; case 5 => _5 ; case 6 => _6 ; case 7 => _7 ; case 8 => _8
      case _ => throw new IllegalArgumentException(s"BombCount must be [0..8]")
    }
}

sealed trait Cell
case object BombCell extends Cell
final case class DigitCell(n: BombCount) extends Cell

final case class Block(cell: Cell, revealed: Boolean)
