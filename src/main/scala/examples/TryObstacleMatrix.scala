package examples

import model.BasicMatrix.*
import model.BasicMatrix.Move.*
import model.QLearning.Pos2d.{Pos, matrix}
import model.{Jumps, Obstacles, OutBoundPenalty}

object TryObstacleMatrix extends App:

  import model.BasicMatrix.*
  import model.QLearning.*

  class M(width: Int,
          height: Int,
          initial: () => Pos = () => Pos(0,0),
          terminal: PartialFunction[Pos, Boolean] ={case _ => false},
          override val reward: PartialFunction[(Pos, Move), Double],
          override val jumps: PartialFunction[((Int,Int), Move), (Int,Int)],
          override val obstacles: Set[(Int,Int)],
          params: LearningParams = LearningParams.default)
    extends Matrix(width, height, initial, terminal, reward,params)
    with Jumps[Pos,Move](jumps)
    with Obstacles[Pos, Move](obstacles)
    with OutBoundPenalty[Pos, Move](width, height)


  @main
    def corridor():Unit =
      val rl = M(7, 3, reward = {
        case (Pos(6,0), RIGHT) => 100
        case _ => 0
        }, jumps = {
        case ((6,0), RIGHT) => (0, 0)
      }, obstacles = Set((1, 0), (1, 1),
        (3, 2), (3,1),
        (5,0), (5,1)
      ))

      val q0 = rl.qFunction
      val q1 = rl.makeLearningInstance().learn(10000, 200, q0)
      val showMatrix = rl.show(matrix(rl.width, rl.height))
      println(showMatrix(s => "%2.0f".format(q0.vFunction(s))))
      println(showMatrix(s => "%2.0f".format(q1.vFunction(s))))
      println(showMatrix(s => q1.bestPolicy(s).toString))

