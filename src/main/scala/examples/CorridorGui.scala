package examples

import examples.TryObstacleMatrix.M
import gui.Cells.*
import gui.MVC.createGui
import model.BasicMatrix.Move.*
import model.QLearning.Pos2d.Pos
import model.Utils.*

import scala.util.Random

object CorridorGui:
  import Random.*

  val w = 19
  val h = 11
  val model = M(w, h,
    initial = () => Pos(0, 3),
    reward = {
      case (Pos(18, 7), RIGHT) => 5
      case (Pos(18, 1), RIGHT) => 10
      case _ => 0
    }, jumps = {
      case ((18, 7), RIGHT) => (0, 9)
      case ((0,9), LEFT) => (0,0)
      case ((18, 9), RIGHT) => (0, 1)
      case ((0, 1), LEFT) => (0, 0)
      case ((18,1), RIGHT) => (0, 3)
    },
  obstacles =
    (square((0,0), w - 1, 2) -- row((0,1), w)) ++
    square((1,3),1,3) ++
    square((4,4),1,3) ++
    square((7,3),1,3) ++
    square((10,4),1,3) ++
    square((13,3),1,3) ++
    square((16,4),1,3) ++
    (square((0,8), w - 1 , 2) -- row((0,9), w)))

  val q0 = model.qFunction

  @main
  def showCorridorLearning():Unit =
    val hist = model.makeLearningInstance().learningHist(q0)(10000, 400)
    createGui(model, 50)(
      borderGrid
      .showStartPos((0,3))
      .showJumps(model.jumps, model.qFunction.actions)
      .showObstacles(model.obstacles))(hist)

  @main
  def showCorridorRuns(): Unit =
    val q1 = model.makeLearningInstance().learn(10000, 700, q0)
    val hist = model.makeLearningInstance().nRuns(q1)(100, 100)(() =>
      val s = randomInitial(model.obstacles, w, h)
      Pos(s._1, s._2))
    createGui(model, 50)(noBorderGrid
      .showJumps(model.jumps, model.qFunction.actions)
      .showObstacles(model.obstacles))(hist)





