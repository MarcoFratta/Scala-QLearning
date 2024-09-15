package model

import model.Items.*
import model.QLearning.{Field, Pos2d}
import Utils.*

object Items:
  trait ItemsState[T] extends Pos2d[T]:
    val items: Set[(Int, Int)]
    def of(items: Set[(Int, Int)]): T


  case class PosWithItems(override val x: Int, override val y: Int,
                     items: Set[(Int, Int)]) extends ItemsState[PosWithItems]:
    override def toString = s"($x,$y, items: ${items.mkString("[", ",", "]")})"
    override def of(p: (Int, Int)): PosWithItems = copy(x = p._1, y = p._2)
    override def of(items: Set[(Int, Int)]): PosWithItems = copy(items = items)

  object PosWithItems:
    def at(p: PosWithItems): Option[(Int, Int, Set[(Int, Int)])] = Some(p.x, p.y, p.items)
    def matrix(w:Int,h:Int, items: Map[Int,(Int, Int)]):Set[PosWithItems] =
      for s <- allItemsCombinations(items.toList.sortBy(_._1).map(_._2)).+(Set())
           row <- 0 until h
           col <- 0 until w
      yield PosWithItems(col, row, s)


  trait Items[S <: ItemsState[S], A]( val items: Map[Int, (Int,Int)] = Map(),
                                      val releasePos: Set[(Int,Int)] = Set(),
                                      val rewardPerItem: Double = 1,
                                      val rewardPerRelease: Double = 10) extends Field[S, A]:
    given Conversion[S, (Int, Int, Set[(Int, Int)])] = p => (p.x, p.y, p.items)

    override def qEnvironment(): Environment = (s: State, a: Action) =>
      (withItems(reward)((s, a)), nextState(s, a))

    override def nextState(s: State, a: Action): State =
      s.of(nextPos((s.x, s.y), a)) match
        case n if isCorrectItem(n) => n.of(n.x, n.y).of(n.items + ((n.x, n.y)))
        case n if canRelease(n) => n.of(n.x, n.y).of(Set()) // empty the set
        case n => n

    private def withItems(r: PartialFunction[(State, Action), Double]):
    PartialFunction[(State, Action), Double] = {
      case (s, a) if isCorrectItem(->(s, a)) => rewardPerItem
      case (s, a) if alreadyPicked(->(s, a)) => Double.MinValue
      case (s, a) if cantRelease(->(s, a)) => Double.MinValue
      case (s, a) if canRelease(->(s, a)) => rewardPerRelease * s.items.size
      case (s, a) => r.apply((s, a))
    }

    private def cantRelease(s:(Int, Int, Set[(Int,Int)])): Boolean =
      releasePos.contains((s._1, s._2)) && s._3 != items.values.toSet

    private def alreadyPicked(s:(Int, Int, Set[(Int,Int)])): Boolean =
      s._3.contains(s._1 -> s._2)

    private def isCorrectItem(s:(Int, Int, Set[(Int,Int)])): Boolean =
      items.applyOrElse(s._3.size, _ => (-1, -1)) == (s._1, s._2)

    private def canRelease(s:(Int, Int, Set[(Int,Int)])): Boolean =
      releasePos.contains((s._1, s._2)) && s._3 == items.values.toSet

    override def show[E](p:Set[State])(v: State => String): String =
      (for s <- allItemsCombinations(items.toList.sortBy(_._1).map(_._2)).+(Set())
        yield "\n Picked up -> " + s.mkString("[", ",", "]") + "\n" +
          p.toList.filter(_.items == s).sortBy(s => (s.y, s.x))
            .sliding(width, width).map(_.map {
            case s if isCorrectItem(s._1, s._2, s._3) => "I "
            case s if canRelease(s) => "R "
            case s => v(s)
          }.mkString("\t")
        ).mkString("\n")).mkString("\n")
