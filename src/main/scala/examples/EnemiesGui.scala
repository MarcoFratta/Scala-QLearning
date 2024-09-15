package examples

import examples.TryEnemyMatrix.M
import gui.Cells.*
import gui.MVC.createGui
import model.BasicMatrix.Move
import model.QLearning.LearningParams
import model.QLearning.Pos2d.{Pos, matrix}

import scala.util.Random

object EnemiesGui:
  val w = 15
  val h = 15
  val params = LearningParams(0.9, 0.5, 0.4, 1)
  val obstacles = Set((0,0),(14,14),(0,14),(14,0))
  val model = M(w, h,
    initial = () => Pos(7, 7),
    reward = {case _ => 17.5},
    enemies = Set((7, 7)),
    obstacles = obstacles,
    params = params)
  val q0 = model.qFunction


  @main
  def showEnemyLearning(): Unit =
    val hist = model.makeLearningInstance().learningHist(q0)(10000, 3000)
    createGui(model, 40)(borderGrid
      //.showStartPos((0,0))
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies))(hist)

  @main
  def showEnemyAfterLearning(): Unit =
    val q1 = model.makeLearningInstance().learn(70000, 1000, q0)
    val hist = model.makeLearningInstance().nRuns(q1)(500, 50)(() =>
      Pos(Random.nextInt(w), Random.nextInt(h)))
    val showMatrix = model.show(matrix(model.width, model.height))
    println(showMatrix(s => q1.bestPolicy(s).toString))
    println(showMatrix(s => "%2.0f".format(q1.vFunction(s))))
    createGui(model, 40)(borderGrid
      .showObstacles(model.obstacles)
      .showEnemies(model.enemies))(hist)
