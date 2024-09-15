package examples

import scala.language.postfixOps
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

object BigMapExample extends App:
  
  val roomSize= 5
  val borderSize = 1
  val nRooms = 6
  val w = roomSize *nRooms + borderSize * (nRooms - 1)
  val h = w
  val itemOffset = (roomSize - 1, roomSize - 1)
  val jumpOffset = (roomSize / 2, roomSize / 2)
  val items = (0 until nRooms).flatMap(i => (0 until nRooms).map(j =>
      (i*nRooms + j) -> (i*(roomSize + borderSize) + itemOffset._1,
        j*(roomSize + borderSize) + itemOffset._2))).toMap
  val obstacles =
    square((0,0), w - 1, h - 1) --
    ((0 until nRooms reverse) flatMap (i =>
      (0 until nRooms reverse) flatMap (j =>
      square((i*(roomSize + borderSize), j*(roomSize + borderSize)), roomSize - 1, roomSize - 1))))

  val jumpsPos = (0 until nRooms).flatMap(i => (0 until nRooms).map(j =>
    (i*(roomSize + borderSize) + jumpOffset._1,
      j*(roomSize + borderSize) + jumpOffset._2))).toSet
  val release = (0,0)
  val model = M(w, h,
    initial = () =>
      val s = randomInitial(obstacles ++ items.values, w, h)
      PosWithItems(s._1, s._2, Set()),
    terminal = {case _ => false},
    reward = {case _ => 0},
    obstacles = obstacles,
    releasePos = Set(release),
    items = items,
    jumps = {
      case ((x,y), DOWN) if jumpsPos.contains((x,y)) => x -> (y + 1 + roomSize + borderSize)
      case ((x,y), RIGHT) if jumpsPos.contains((x,y)) => (x + 1 + roomSize + borderSize) -> y
      case ((x,y), UP) if jumpsPos.contains((x,y)) => x -> (y - 1 - roomSize - borderSize)
      case ((x,y), LEFT) if jumpsPos.contains((x,y)) => (x - 1 - roomSize - borderSize) -> y
    },
    rewardPerItem = 10,
    rewardPerRelease = 100)
  val q0 = model.qFunction


  @main
  def showBigMapLearning():Unit =
    val hist = model.makeLearningInstance().learningHist(q0)(10000, 750)
    createGui(model, 20)(borderGrid
      //.showStartPos((0,0))
      .showJumps(model.jumps, model.qFunction.actions)
      .showItems(model.items.values.toSet, model.releasePos)
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies))(hist)

  @main
  def showBigMapAfterLearning(): Unit =
    val q1 = model.makeLearningInstance().learn(10000, 1000, q0)
    val hist = model.makeLearningInstance().nRuns(q1)(500, 1000)(() =>
     val p = randomInitial(model.obstacles, w,h)
      PosWithItems(p._1, p._2, Set()))
    val showMatrix = model.show(matrix(model.width, model.height, items))
    println(showMatrix(s => q1.bestPolicy(s).toString))
    createGui(model, 10)(borderGrid
      //.showStartPos((0,0))
      .showItems(model.items.values.toSet,model.releasePos)
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies)
      .showJumps(model.jumps, model.qFunction.actions))(hist)
