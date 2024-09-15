package examples

import model.QLearning.Pos2d.{Pos, matrix}
import model.{Enemies, Jumps, Obstacles, OutBoundPenalty}

object TryEnemyMatrix extends App:

  import model.BasicMatrix.*
  import model.QLearning.*

  class M(width: Int,
          height: Int,
          initial: () => Pos = () => Pos(0, 0),
          terminal: PartialFunction[Pos, Boolean] = {case _ => false},
          override val reward: PartialFunction[(Pos, Move), Double],
          override val jumps: PartialFunction[((Int, Int), Move), (Int, Int)] = Map.empty,
          obstacles: Set[(Int, Int)] = Set(),
          override val enemies: Set[(Int, Int)] = Set(),
          params: LearningParams = LearningParams.default)
    extends Matrix(width, height, initial, terminal, reward, params)
      with Obstacles[Pos, Move](obstacles)
      with Jumps[Pos, Move](jumps)
      with OutBoundPenalty[Pos, Move](width, height)
      with Enemies[Pos, Move](enemies, Math.sqrt(width*width + height*height))


  @main
  def singleEnemy(): Unit =
    val rl = M(7, 7,
      reward = {case _ => 0},
      enemies = Set((3, 3)))

    val q0 = rl.qFunction
    val q1 = rl.makeLearningInstance().learn(100000, 500, q0)
    val showMatrix = rl.show(matrix(rl.width,rl.height))
    println(showMatrix(s => "%2.0f".format(q0.vFunction(s))))
    println(showMatrix(s => "%2.0f".format(q1.vFunction(s))))
    println(showMatrix(s => q1.bestPolicy(s).toString))


  @main
  def multipleEnemies(): Unit =
    val rl = M(7, 7,
      reward = {
        case _ => 0
      }, obstacles = Set((6, 0), (6, 1), (6, 2), (5, 2), (4, 2)),
      enemies = Set((3, 3)))

    val q0 = rl.qFunction
    val q1 = rl.makeLearningInstance().learn(100000, 700, q0)
    val showMatrix = rl.show(matrix(rl.width,rl.height))
    println(showMatrix(s => "%2.0f".format(q0.vFunction(s))))
    println(showMatrix(s => "%2.0f".format(q1.vFunction(s))))
    println(showMatrix(s => q1.bestPolicy(s).toString))

