package examples

import model.BasicMatrix.*
import model.BasicMatrix.Move.*
import model.Items.{Items, ItemsState, PosWithItems}
import model.QLearning.{LearningParams, Pos2d}
import model.{Enemies, Jumps, Obstacles, OutBoundPenalty}



object TryItemMatrix extends App:
  import model.Items.PosWithItems.matrix

  class M[S <: ItemsState[S]](width: Int,
                              height: Int,
                              initial: () => S,
                              terminal: PartialFunction[S, Boolean],
                              override val reward: PartialFunction[(S, Move), Double],
                              jumps: PartialFunction[((Int,Int), Move), (Int,Int)],
                              releasePos: Set[(Int,Int)] = Set(),
                              items: Map[Int,(Int,Int)] = Map(),
                              rewardPerItem: Double = 1,
                              rewardPerRelease: Double = 10,
                              obstacles: Set[(Int,Int)] = Set(),
                              override val enemies: Set[(Int, Int)] = Set(),
                              val enemyPenalty:Double = 0,
                              params: LearningParams = LearningParams.default)
    extends Matrix[S](width, height, initial, terminal, reward, params)
      with Items[S, Move](items,releasePos, rewardPerItem, rewardPerRelease)
      with Obstacles[S, Move](obstacles)
      with Jumps[S, Move](jumps)
      with OutBoundPenalty[S, Move](width, height)
      with Enemies[S, Move](enemies, enemyPenalty)


  @main
  def withObstaclesAndJumps(): Unit =
    val items = Map(0 -> (4,4))
    val obstacles = Set((3,2),(3,1),(3,0),(3,3),(4,3))
    val rl = M(5, 5,
      initial =  () => PosWithItems(0,0,Set()),
      terminal = {
        case x if obstacles.contains((x.x,x.y)) =>
          println("Obstacle hit")
          true
        case _ => false},
      reward = {case _ => 0},
      jumps = {
        case ((2, 0), RIGHT) => (4, 0)
      },
      obstacles = obstacles,
      releasePos = Set((4,2)),
      items = items,
      rewardPerItem = 0,
      rewardPerRelease = 1)

    val q0 = rl.qFunction
    val showMatrix = rl.show(matrix(rl.width, rl.height, items))
    println(showMatrix(s => "%2.1f".format(q0.vFunction(s))))
    val q1 = rl.makeLearningInstance().learn(10000, 100, q0)
    println(showMatrix(s => "%2.1f".format(q1.vFunction(s))))
    println(showMatrix(rl.printJumps(q1)))

  @main
  def multipleItems():Unit =
    val params = LearningParams(0.9, 0.5, 0.45, 0)
    val items = Map(0 -> (5,0), 1 ->  (0,5))
    val rl = M(6, 6,
      reward = {case _ => 0},
      jumps = Map.empty,
      initial = () => PosWithItems(0, 0, Set()),
      terminal = {case _ => false},
      releasePos = Set((5, 5)),
      items = items,
      rewardPerItem = 0,
      rewardPerRelease = 1)

    val q0 = rl.qFunction
    val showMatrix = rl.show(matrix(rl.width, rl.height, items))
    println(showMatrix(s => "%2.1f".format(q0.vFunction(s))))
    val q1 = rl.makeLearningInstance().learn(10000, 500, q0)
    println(showMatrix(s => "%2.1f".format(q1.vFunction(s))))
    println(showMatrix(s => q1.bestPolicy(s).toString))
