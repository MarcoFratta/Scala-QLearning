package model

import model.QLearning.*

import model.Utils.*

trait OutBoundPenalty[S <: Pos2d[S], A](width:Int, height:Int) extends Field[S, A] :
  override def qEnvironment(): Environment = (s: S, a: A) => (s, a) match
    case (n, m) if isOutsideBounds(nextPos(n.pos, m)) => (Double.MinValue, n)
    case _ => super.qEnvironment()(s, a)

  private def isOutsideBounds(p: (Int,Int)): Boolean =
    p._1 < 0 || p._1 >= width || p._2 < 0 || p._2 >= height