package examples

import examples.TryItemMatrix.M
import gui.Cells.*
import gui.MVC.createGui
import model.BasicMatrix.*
import model.BasicMatrix.Move.*
import model.Items.PosWithItems
import model.Items.PosWithItems.matrix
import model.QLearning.LearningParams
import model.Utils.*

import scala.util.Random

object ItemsGui extends App:

  val w = 15
  val h = 15
  val items = Map(0 -> (13, 0), 1 -> (14,11), 2 -> (3,3))
  val obstacles = col((12,0),5) ++ Set(
    (13,4),
    (14,10),(13,10),(13,11),(13,12),(13,12),(13,13),(12,13),(11,13),
    (3,2),(2,2),(2,3),(2,4),(3,4),(4,4),(5,4),(5,3),(5,2),(5,1),(5,0),
    (4,14),(4,13),(4,12),(4,11),(3,11),(2,11),(1,11))
  val release = (3,13)
  val params = LearningParams(0.9, 0.5, 0.3, 1)
  val jumps:PartialFunction[((Int, Int), Move), (Int, Int)] = {
    case ((10, 0), UP) => (14, 0)
  }
  val model = M(w, h,
    initial = () =>
    val p = randomInitial(obstacles ++ items.values.toSet, w, h)
    PosWithItems(p._1, p._2, Set()),
    terminal = {case _ => false},
    reward = {case _ => 0},
    obstacles = obstacles,
    releasePos = Set(release),
    items = items,
    jumps = jumps,
    rewardPerItem = 100,
    rewardPerRelease = 1000,
    enemyPenalty = 0.3,
    enemies = Set((7,7)),
    params = params)
  val q0 = model.qFunction


  @main
  def showLearning():Unit =
    val hist = model.makeLearningInstance().learningHist(q0)(10000, 750)
    createGui(model, 40)(borderGrid
      //.showStartPos((0,0))
      .showItems(model.items.values.toSet, model.releasePos)
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies))(hist)

  @main
  def showAfterLearning(): Unit =
    val q1 = model.makeLearningInstance().learn(10000, 700, q0)
    val hist = model.makeLearningInstance().nRuns(q1)(500, 200)(() =>
      PosWithItems(Random.nextInt(w),Random.nextInt(h), Set()))
    val showMatrix = model.show(matrix(model.width, model.height, items))
    println(showMatrix(s => q1.bestPolicy(s).toString))
    createGui(model, 40)(borderGrid
      //.showStartPos((0,0))
      .showItems(model.items.values.toSet,model.releasePos)
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies)
      .showJumps(jumps, model.qFunction.actions))(hist)
