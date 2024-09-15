package examples

import model.BasicMatrix.*
import model.BasicMatrix.Move.*

import model.QLearning.Pos2d.Pos
import model.QLearning.Pos2d.matrix
import model.{Jumps, Obstacles, OutBoundPenalty}

object TryBasicMatrix extends App:

  import model.BasicMatrix.*
  import model.QLearning.*
  import Pos.*



  @main
  def basicMatrix():Unit =
    val rl = Matrix( width = 5,
      height = 5,
      initial = () => Pos(0,0),
      terminal = {case _=> false},
      reward = { 
        case (Pos(1,0), DOWN) => 10; 
        case (Pos(3,0), DOWN) => 5; 
        case _ => 0}
    )

    val q0 = rl.qFunction
    val q1 = rl.makeLearningInstance().learn(10000, 200, q0)
    val showMatrix = rl.show(matrix(rl.width,rl.height))
    println(showMatrix(s => "%2.0f".format(q0.vFunction(s))))
    println(showMatrix(s => "%2.0f".format(q1.vFunction(s))))
    println(showMatrix(s => q1.bestPolicy(s).toString))

