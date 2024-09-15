package gui

import cats.*
import cats.data.*
import cats.implicits.*
import gui.ModelState.*

import scala.annotation.targetName
import gui.Cells.*
import model.QLearning.{Field, Pos2d}
import model.Utils.*

object MVC:
  import View.*

  // Loop until the condition is false
  private def loop[A](cond: State[A, Boolean])(delay:Int): State[A, Unit] = {
    cond flatMap { bool =>
      if (bool)
        Thread.sleep(delay)
        loop(cond)(delay)
      else State.empty
    }
  }

  private def getModel[S <: Pos2d[S]]:State[(ModelState[S], Window), (ModelState[S], Window)] =
    State(s => (s, s))
  private def checkFinish[S <: Pos2d[S]]:State[(ModelState[S], Window), Boolean] =
    State(s => (s, true))
  private def createApp[S <: Pos2d[S]](v:State[Window, Unit]):State[(ModelState[S], Window), Window] =
    State.apply(s =>
      val view = v.run(s._2).value._1
      ((s._1,view ), view))

  extension (s:State[Window,Unit])
    private def transformed[A]:State[(A, Window), Unit] = s.transformS[(A, Window)](r => r._2, (a, b) => a)
    @targetName("alias for transformed")
    private def ![A]:State[(A, Window), Unit] = s.transformed[A]
    private def ifTrue[S <: Pos2d[S]](b:Boolean): State[(ModelState[S], Window), Unit] =
      if b then s.transformed else State.empty
  private def modelView[S <: Pos2d[S]](m:ModelState[S], v:Window): State[(ModelState[S], Window), Unit] = State.set((m,v))
  private def isNewEpisode[S <: Pos2d[S]](s: ModelState[S], newModel: ModelState[S]) = s.episode != newModel.episode

  def createGui[S <: Pos2d[S],A](f:Field[S, A], cellSize:Int)(mapper:(Int,Int) => Cell)
                                (hist: LazyList[LazyList[(S,f.Q)]]): Unit =
    given Int = 50
    val initWindow = for
      _ <- withSize(cellSize * f.width, cellSize * f.height + 250)
      _ <- addGrid(f.width, f.height)(cellSize)
      _ <- addButton("prevButton", "Prev")
      _ <- addButton("stopButton", "Play/Stop")
      _ <- addButton("nextButton", "Next")
      _ <- updateAll(mapper)
      v <- show()
    yield v

    val bestAction = (s:(S,f.Q)) => "Best action: " +
      s._2.actions.map(st => st -> s._2(s._1, st)).maxBy(_._2)._1

    val updateLabels:ModelState[S] => State[Window, Unit] = m =>
      for
        _ <- updateLabel("ELength", "Episode length -> " + (m.step + 1))
        _ <- updateLabel("state", "State -> " + hist(m.episode)(m.step)._1)
        _ <- updateLabel("episode", "Episode -> " + (m.episode + 1))
        _ <- updateLabel("policy", bestAction(hist(m.episode)(m.step)))
      yield ()

    val handlePrev = for
      (s, v) <- getModel[S]
      (x, y) = s.getState(hist)
      _ <- select(x, y)(mapper(x, y)).!
      newModel = s.prev(hist)
      (newX, newY) = newModel.getState(hist)
      _ <- updateAll(mapper).ifTrue(isNewEpisode(s, newModel))
      _ <- selectN(newModel.path.map(_.pos).toSet)(path).ifTrue(isNewEpisode(s, newModel))
      _ <- select(newX, newY)(robot).!
      _ <- updateLabels(newModel).!
      _ <- pressButton("prevButton").ifTrue(v.isAutoplay)
      _ <- State.set((newModel, v))
    yield ()

    val handleNext = for
      (s, v) <- getModel[S]
      (x, y) = s.getState(hist)
      _ <- select(x, y)(path).!
      newModel = s.next(hist)
      (newX, newY) = newModel.getState(hist)
      _ <- select(newX, newY)(robot).!
      _ <- updateAll(mapper).ifTrue(isNewEpisode(s, newModel))
      _ <- updateLabels(newModel).!
      _ <- pressButton("nextButton").ifTrue(v.isAutoplay)
      _ <- State.set((newModel, v))
    yield ()

    val app = for
       events <- eventStream()
       _ <- handle(events) {
          case "prevButton" => handlePrev
          case "nextButton" => handleNext
          case "stopButton" => (for
              _ <- autoplay()
            yield ()).!
       }
    yield true // act as the while loop condition

    val w = initWindow.run(defaultWindow("MVC")).value._1
    loop(app)(30).run((ModelState(0,0,List()), w)).value._1







