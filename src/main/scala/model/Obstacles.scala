package model

import model.QLearning.*
import model.Utils.*

trait Obstacles[S <: Pos2d[S], A](val obstacles: Set[(Int,Int)]) extends Field[S, A]:
    override def show[E](p:Set[State])(v: S => String): String =
      super.show(p)(s => if isObstacle(s.pos) then "X " else v(s))

    override def qEnvironment(): Environment = (s: S, a: A) => if isObstacle(nextPos(s.pos, a))
        then (Double.MinValue, s)
        else super.qEnvironment()(s, a)

    private def isObstacle(s: (Int, Int)): Boolean = obstacles.contains(s)
