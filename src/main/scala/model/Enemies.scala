package model

import model.QLearning.*
import model.Utils.*

trait Enemies[S <: Pos2d[S], A](val enemies:Set[(Int,Int)],penalty:Double) extends Field[S, A]:

    override def show[E](p:Set[State])(v: S => String): String = super.show(p)(s =>
        if enemies.contains(s.pos) then "E " else v(s))

    override def qEnvironment(): Environment = (s: S, a: A) =>
        val r = super.qEnvironment()(s, a)
        (s, a) match
            case (n,p) if isAnEnemy(nextPos(n.pos, p)) => (Double.MinValue, r._2)
            case (n,p) if enemies.nonEmpty => (r._1 - (penalty / totalDistanceFromEnemies(s.pos)), r._2)
            case _ => r

    private def totalDistanceFromEnemies(p: (Int, Int)): Double =
        enemies.map(p2 => distance(p, p2)).sum

    private def isAnEnemy(p: (Int, Int)): Boolean = enemies.contains(p)
    private def distance(p1: (Int, Int), p2: (Int, Int)): Double = {
        val dx = p1._1 - p2._1
        val dy = p1._2 - p2._2
        Math.sqrt(dx * dx + dy * dy)
    }

