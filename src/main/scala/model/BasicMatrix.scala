package model

import model.QLearning.*

import model.Utils.*
object BasicMatrix:

  enum Move:
    case LEFT, RIGHT, UP, DOWN

    override def toString = Map(LEFT -> "<", RIGHT -> ">",
      UP -> "^", DOWN -> "v")(this)

  import Move.*

  case class Matrix[S <: Pos2d[S]]
  (override val width: Int,
   override val height: Int,
   override val initial: () => S,
   override val terminal: PartialFunction[S, Boolean],
   override val reward: PartialFunction[(S, Move), Double],
   override val params: LearningParams = LearningParams.default) extends Field[S, Move]:

    override type State = S
    override type Action = Move

    override def nextPos(p: (Int, Int), m: Move): (Int, Int) = m match
          case LEFT => (0 max p._1 - 1, p._2)
          case RIGHT => ((width-1) min p._1 + 1, p._2)
          case UP => (p._1, 0 max p._2 - 1)
          case DOWN => (p._1, (height-1) min p._2 + 1)
    
    override def show[E](p:Set[State])(v: State => String): String =
      p.toList.sortBy(s => (s.y, s.x)).sliding(width, width)
        .map(_.map(v).mkString("\t")).mkString("\n")


    override def qFunction: Q = QFunction(Move.values.toSet, params.v0, terminal)