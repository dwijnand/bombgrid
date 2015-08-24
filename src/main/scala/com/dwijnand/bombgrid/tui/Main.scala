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

    val bombNoBombs = Grid.create(size)((x, y) => if (bombsCoords((x, y))) IsBomb else NoBomb)

    val bombCounts = BombCount fromGrid bombNoBombs

    val grid =
      bombCounts map { bc =>
        val cell = bc.map(DigitCell).getOrElse(BombCell)
        Block(cell, revealed = true)
      }

    grid foreach { row =>
      println(row.map { b =>
        if (b.revealed) b.cell match {
          case BombCell               => BLACK + BOLD + "*" + RESET
          case DigitCell(BombCount_0) => " "
          case DigitCell(BombCount_1) => BLUE + BOLD + "1" + RESET
          case DigitCell(BombCount_2) => GREEN + "2" + RESET
          case DigitCell(BombCount_3) => RED + BOLD + "3" + RESET
          case DigitCell(BombCount_4) => BLUE + "4" + RESET
          case DigitCell(BombCount_5) => RED + "5" + RESET
          case DigitCell(BombCount_6) => CYAN + "6" + RESET
          case DigitCell(BombCount_7) => MAGENTA + "7" + RESET
          case DigitCell(BombCount_8) => BOLD + "8" + RESET
        } else "#"
      }.mkString)
    }

    ()
  }
}

sealed trait BombNoBomb
case object IsBomb extends BombNoBomb
case object NoBomb extends BombNoBomb

final class Grid[+T] private (val size: (Int, Int), val rows: IndexedSeq[IndexedSeq[T]]) {
  def map[U](f: T => U) = new Grid(size, rows map (_ map f))
  def foreach[U](f: IndexedSeq[T] => U) = rows foreach f
  def apply(idx: Int) = rows(idx)
  def mapWithCoords[U](f: (Int, Int, T) => U) = {
    val newRows =
      rows.indices map { y =>
        val row = rows(y)
        row.indices map { x =>
          f(x, y, row(x))
        }
      }
    new Grid(size, newRows)
  }

}

object Grid {
  def create[T](size: (Int, Int))(f: (Int, Int) => T): Grid[T] = {
    val rows =
      0 until size._2 map { y =>
        0 until size._1 map { x =>
          f(x, y)
        }
      }
    new Grid(size, rows)
  }
}

sealed trait BombCount extends Any { def value: Int }
case object BombCount_0 extends BombCount { val value = 0 }
case object BombCount_1 extends BombCount { val value = 1 }
case object BombCount_2 extends BombCount { val value = 2 }
case object BombCount_3 extends BombCount { val value = 3 }
case object BombCount_4 extends BombCount { val value = 4 }
case object BombCount_5 extends BombCount { val value = 5 }
case object BombCount_6 extends BombCount { val value = 6 }
case object BombCount_7 extends BombCount { val value = 7 }
case object BombCount_8 extends BombCount { val value = 8 }

object BombCount {
  def fromGrid(grid: Grid[BombNoBomb]): Grid[Option[BombCount]] = {
    val (size_x, size_y) = grid.size

    def sumBombs(grid: Grid[BombNoBomb], xy: (Int, Int)): BombCount = {
      val (x, y) = xy

      val ul = if (y > 0 && x > 0)          grid(y - 1)(x - 1) else NoBomb
      val uc = if (y > 0)                   grid(y - 1)(x)     else NoBomb
      val ur = if (y > 0 && x + 1 < size_x) grid(y - 1)(x + 1) else NoBomb

      val cl = if (x > 0)          grid(y)(x - 1) else NoBomb
      val cr = if (x + 1 < size_x) grid(y)(x + 1) else NoBomb

      val dl = if (y + 1 < size_y && x > 0)          grid(y + 1)(x - 1) else NoBomb
      val dc = if (y + 1 < size_y)                   grid(y + 1)(x)     else NoBomb
      val dr = if (y + 1 < size_y && x + 1 < size_x) grid(y + 1)(x + 1) else NoBomb

      BombCount fromInt Vector(ul, uc, ur, cl, cr, dl, dc, dr).count(_ == IsBomb)
    }

    grid mapWithCoords {
      case (x, y, NoBomb) => Some(sumBombs(grid, (x, y)))
      case (_, _, IsBomb) => None
    }
  }

  private def fromInt(n: Int) =
    n match {
      case 0 => BombCount_0
      case 1 => BombCount_1 ; case 2 => BombCount_2 ; case 3 => BombCount_3 ; case 4 => BombCount_4
      case 5 => BombCount_5 ; case 6 => BombCount_6 ; case 7 => BombCount_7 ; case 8 => BombCount_8
      case _ => throw new IllegalArgumentException(s"BombCount must be [0..8]")
    }
}

sealed trait Cell
case object BombCell extends Cell
final case class DigitCell(n: BombCount) extends Cell

final case class Block(cell: Cell, revealed: Boolean)
