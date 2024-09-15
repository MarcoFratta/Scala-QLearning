package model

import model.QLearning.*
import scala.util.Random

object Utils {

  def samePosition[S <: Pos2d[S]](s1:S,s2:S): Boolean =
    s1.x == s2.x && s1.y == s2.y
  def isPresent[S <: Pos2d[S]](p:(Int,Int), set: Set[S]): Boolean =
    set.exists(s2 => s2.x == p._1 && s2.y == p._2)
  def square(topLeft:(Int,Int), xEnd:Int, yEnd:Int): Set[(Int,Int)] =
    (topLeft._1 to topLeft._1 + xEnd).flatMap(x =>
      (topLeft._2 to topLeft._2 + yEnd).map(y => (x,y))).toSet
  def row(p:(Int,Int), w:Int): Set[(Int,Int)] = square(p, w - 1, 0)
  def col(p:(Int,Int), h:Int): Set[(Int,Int)] = square(p, 0, h - 1)
  def randomInitial(o:Set[(Int,Int)], w:Int, h:Int): (Int,Int) =
    Random.shuffle(square((0,0), w - 1, h - 1).diff(o).toList).head
  def allItemsCombinations(l: List[(Int, Int)]): Set[Set[(Int, Int)]] = l.size match
    case 0 => Set()
    case _ => Set(l.toSet).union(allItemsCombinations(l.dropRight(1)))
  extension[S](v:Pos2d[S])
    def pos: (Int, Int) = (v.x, v.y)

}



